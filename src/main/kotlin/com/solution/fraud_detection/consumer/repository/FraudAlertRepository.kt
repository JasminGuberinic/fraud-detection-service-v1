package com.solution.fraud_detection.consumer.repository

import com.solution.fraud_detection.consumer.model.FraudAlertElk
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface FraudAlertRepository : ElasticsearchRepository<FraudAlertElk, String> {
    fun findByAccountId(accountId: String): List<FraudAlertElk>
    fun findByTimestampBetween(startTime: LocalDateTime, endTime: LocalDateTime): List<FraudAlertElk>
}