package com.solution.fraud_detection.consumer.mapper

import com.solution.fraud_detection.common.model.FraudAlertType
import com.solution.fraud_detection.consumer.model.FraudAlertElk
import com.solution.fraud_detection.common.model.FraudAlert
import com.solution.fraud_detection.consumer.controller.FraudAlertResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun FraudAlert.toElk(): FraudAlertElk {
    return FraudAlertElk(
        id = this.id,
        accountId = this.accountId,
        transactionId = this.transactionId,
        type = this.type,
        description = this.description,
        timestamp = this.timestamp,
        createdAt = LocalDateTime.now(),
        processed = false,
        severity = calculateSeverity()
    )
}

fun FraudAlertElk.toFraudAlert(): FraudAlert {
    return FraudAlert(
        id = this.id,
        accountId = this.accountId,
        transactionId = this.transactionId,
        type = this.type,
        description = this.description,
        timestamp = this.timestamp
    )
}

private fun FraudAlert.calculateSeverity(): AlertSeverity {
    return when (this.type) {
        FraudAlertType.HIGH_AMOUNT -> AlertSeverity.HIGH
        FraudAlertType.MULTIPLE_TRANSACTIONS -> AlertSeverity.MEDIUM
        FraudAlertType.SUSPICIOUS_LOCATION -> AlertSeverity.HIGH
        else -> AlertSeverity.LOW
    }
}

fun FraudAlertElk.toResponse(): FraudAlertResponse {
    return FraudAlertResponse(
        id = this.id,
        accountId = this.accountId,
        transactionId = this.transactionId,
        type = this.type.toString(),
        description = this.description,
        timestamp = this.timestamp.format(DateTimeFormatter.ISO_DATE_TIME),
        severity = this.severity.toString(),
        createdAt = this.createdAt.format(DateTimeFormatter.ISO_DATE_TIME)
    )
}

enum class AlertSeverity {
    LOW,
    MEDIUM,
    HIGH
}