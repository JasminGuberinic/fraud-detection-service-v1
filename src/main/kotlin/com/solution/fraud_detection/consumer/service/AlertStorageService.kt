package com.solution.fraud_detection.consumer.service

import com.solution.fraud_detection.consumer.model.FraudAlertElk
import com.solution.fraud_detection.consumer.repository.FraudAlertRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AlertStorageService(
    private val fraudAlertRepository: FraudAlertRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun saveAlert(alert: FraudAlertElk): FraudAlertElk {
        logger.info("Saving fraud alert to Elasticsearch: ${alert.id}")
        return fraudAlertRepository.save(alert)
    }

    fun findById(id: String): FraudAlertElk? {
        return fraudAlertRepository.findById(id).orElse(null)
    }

    fun findByAccountId(accountId: String): List<FraudAlertElk> {
        return fraudAlertRepository.findByAccountId(accountId)
    }

    fun findByTimeRange(startTime: LocalDateTime, endTime: LocalDateTime): List<FraudAlertElk> {
        return fraudAlertRepository.findByTimestampBetween(startTime, endTime)
    }

    fun findAllPaged(pageable: Pageable): Page<FraudAlertElk> {
        return fraudAlertRepository.findAll(pageable)
    }

    fun deleteAlert(id: String) {
        fraudAlertRepository.deleteById(id)
    }
}