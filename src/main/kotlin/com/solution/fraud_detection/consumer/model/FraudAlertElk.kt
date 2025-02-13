package com.solution.fraud_detection.consumer.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.solution.fraud_detection.common.model.FraudAlertType
import com.solution.fraud_detection.consumer.mapper.AlertSeverity
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.time.LocalDateTime
import org.springframework.data.elasticsearch.annotations.DateFormat

/**
 * Model class representing a document in Elasticsearch.
 * Each instance will be stored as a JSON document in the fraud-alerts index.
 */
@Document(indexName = "fraud-alerts")
data class FraudAlertElk(
    /**
     * Document ID in Elasticsearch.
     * Similar to primary key in traditional databases.
     */
    @Id
    val id: String,

    /**
     * Keyword type is optimized for exact matches and aggregations.
     * Good for IDs, enums, precise values.
     */
    @Field(type = FieldType.Keyword)
    val accountId: String,

    @Field(type = FieldType.Keyword)
    val transactionId: String,

    @Field(type = FieldType.Keyword)
    val type: FraudAlertType,

    /**
     * Text type is analyzed and tokenized.
     * Good for full-text search in descriptions.
     */
    @Field(type = FieldType.Text)
    val description: String,

    /**
     * Date type enables range queries and date operations.
     * Stored in UTC format internally.
     */
    @Field(type = FieldType.Date, format = [DateFormat.basic_date_time])
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val timestamp: LocalDateTime,

    @Field(type = FieldType.Date, format = [DateFormat.basic_date_time])
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime,

    @Field(type = FieldType.Boolean)
    val processed: Boolean = false,

    @Field(type = FieldType.Keyword)
    val severity: AlertSeverity
)
