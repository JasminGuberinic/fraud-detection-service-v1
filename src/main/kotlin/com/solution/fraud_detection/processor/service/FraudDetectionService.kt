package com.solution.fraud_detection.processor.service

import com.solution.fraud_detection.common.model.FraudAlert
import com.solution.fraud_detection.common.model.FraudAlertType
import com.solution.fraud_detection.common.model.Transaction
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class FraudDetectionService(
    private val alertService: AlertService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${fraud.detection.amount.threshold:1000.0}")
    private var amountThreshold: Double = 1000.0

    @Value("\${fraud.detection.suspicious.locations}")
    private lateinit var suspiciousLocations: List<String>

    fun analyzeTransaction(transaction: Transaction): FraudAlert? {
        val alert = when {
            isHighAmount(transaction) -> createAlert(
                transaction,
                FraudAlertType.HIGH_AMOUNT,
                "Transaction amount ${transaction.amount} exceeds threshold $amountThreshold"
            )

            isSuspiciousLocation(transaction) -> createAlert(
                transaction,
                FraudAlertType.SUSPICIOUS_LOCATION,
                "Transaction location ${transaction.location} is suspicious"
            )

            else -> null
        }

        alert?.let { alertService.sendAlert(it) }
        return alert
    }

    private fun isHighAmount(transaction: Transaction): Boolean =
        transaction.amount > amountThreshold

    private fun isSuspiciousLocation(transaction: Transaction): Boolean =
        suspiciousLocations.contains(transaction.location.toLowerCase())

    private fun createAlert(
        transaction: Transaction,
        type: FraudAlertType,
        description: String
    ): FraudAlert {
        return FraudAlert(
            id = UUID.randomUUID().toString(),
            transactionId = transaction.id,
            type = type,
            description = description,
            timestamp = LocalDateTime.now(),
            accountId = transaction.accountId
        )
    }
}