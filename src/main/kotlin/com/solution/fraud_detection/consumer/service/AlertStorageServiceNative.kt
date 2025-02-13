package com.solution.fraud_detection.consumer.service

import com.solution.fraud_detection.consumer.model.FraudAlertElk
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.StringQuery
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime

@Service
class AlertStorageServiceNative(
    private val elasticsearchOperations: ElasticsearchOperations
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val indexName = "fraud-alerts"

    fun searchWithStringQuery(
        accountId: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        page: Int,
        size: Int
    ): Page<FraudAlertElk> {
        val queryString = """
        {
            "bool": {
                "must": [
                    ${accountId?.let { """{"term": {"accountId": "$it"}}""" } ?: ""},
                    ${if (startTime != null && endTime != null) """
                        {
                            "range": {
                                "timestamp": {
                                    "gte": "$startTime",
                                    "lte": "$endTime"
                                }
                            }
                        }
                    """ else ""}
                ]
            }
        }
        """

        val query = StringQuery(
            queryString.replace(", ]", "]"), // Clean up empty elements
            PageRequest.of(page, size)
        )

        val searchHits = elasticsearchOperations.search(
            query,
            FraudAlertElk::class.java,
            IndexCoordinates.of(indexName)
        )

        return PageImpl(
            searchHits.searchHits.map { it.content },
            PageRequest.of(page, size),
            searchHits.totalHits
        )
    }

    fun searchWithComplexQuery(searchText: String?, minAmount: Double?): Page<FraudAlertElk> {
        val queryString = """
        {
            "bool": {
                "should": [
                    {
                        "match": {
                            "description": {
                                "query": "$searchText",
                                "fuzziness": "AUTO"
                            }
                        }
                    },
                    {
                        "match": {
                            "location": "$searchText"
                        }
                    }
                ],
                "filter": [
                    ${minAmount?.let { """
                        {
                            "range": {
                                "amount": {
                                    "gte": $it
                                }
                            }
                        }
                    """ } ?: ""}
                ],
                "minimum_should_match": 1
            }
        }
        """

        val query = StringQuery(
            queryString.replace(", ]", "]"),
            PageRequest.of(0, 10)
        )

        val searchHits = elasticsearchOperations.search(
            query,
            FraudAlertElk::class.java,
            IndexCoordinates.of(indexName)
        )

        return PageImpl(
            searchHits.searchHits.map { it.content },
            PageRequest.of(0, 10),
            searchHits.totalHits
        )
    }

    fun searchWithAggregations(
        timeFrame: LocalDateTime,
        minAmount: Double?,
        page: Int,
        size: Int
    ): Page<FraudAlertElk> {
        val queryString = """
        {
            "bool": {
                "must": [
                    {
                        "range": {
                            "timestamp": {
                                "gte": "$timeFrame"
                            }
                        }
                    }
                ],
                "should": [
                    {
                        "range": {
                            "amount": {
                                "gte": ${minAmount ?: 1000}
                            }
                        }
                    }
                ],
                "minimum_should_match": 1,
                "aggs": {
                    "fraud_types": {
                        "terms": {
                            "field": "type"
                        },
                        "aggs": {
                            "avg_amount": {
                                "avg": {
                                    "field": "amount"
                                }
                            }
                        }
                    }
                }
            }
        }
        """

        val query = StringQuery(
            queryString.replace(", ]", "]"),
            PageRequest.of(page, size)
        )

        return executeSearch(query)
    }

    fun searchByLocationPattern(
        locations: List<String>,
        timeWindow: Duration,
        page: Int,
        size: Int
    ): Page<FraudAlertElk> {
        val queryString = """
        {
            "bool": {
                "must": [
                    {
                        "range": {
                            "timestamp": {
                                "gte": "now-${timeWindow.toHours()}h"
                            }
                        }
                    }
                ],
                "should": [
                    {
                        "terms": {
                            "location": ${locations.map { "\"$it\"" }}
                        }
                    }
                ],
                "minimum_should_match": 1,
                "aggs": {
                    "locations": {
                        "terms": {
                            "field": "location",
                            "size": 10
                        }
                    },
                    "hourly": {
                        "date_histogram": {
                            "field": "timestamp",
                            "calendar_interval": "hour"
                        }
                    }
                }
            }
        }
        """

        val query = StringQuery(
            queryString.replace(", ]", "]"),
            PageRequest.of(page, size)
        )

        return executeSearch(query)
    }

    fun searchMultiFieldPattern(
        searchPattern: String,
        fields: List<String> = listOf("description", "location", "transactionId"),
        page: Int,
        size: Int
    ): Page<FraudAlertElk> {
        val queryString = """
        {
            "multi_match": {
                "query": "$searchPattern",
                "fields": ${fields.map { "\"$it\"" }},
                "type": "best_fields",
                "fuzziness": "AUTO",
                "operator": "and"
            }
        }
        """

        val query = StringQuery(
            queryString.replace(", ]", "]"),
            PageRequest.of(page, size)
        )

        return executeSearch(query)
    }

    private fun executeSearch(query: StringQuery): Page<FraudAlertElk> {
        val searchHits = elasticsearchOperations.search(
            query,
            FraudAlertElk::class.java,
            IndexCoordinates.of(indexName)
        )

        return PageImpl(
            searchHits.searchHits.map { it.content },
            query.pageable,
            searchHits.totalHits
        )
    }
}