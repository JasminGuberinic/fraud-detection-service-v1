package com.solution.fraud_detection.common.model

enum class FraudAlertType {
    HIGH_AMOUNT,
    SUSPICIOUS_LOCATION,
    MULTIPLE_TRANSACTIONS;

    override fun toString(): String = name.lowercase()
}