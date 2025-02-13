package com.solution.fraud_detection.consumer.controller

data class FraudAlertResponse(
    val id: String,
    val accountId: String,
    val transactionId: String,
    val type: String,
    val description: String,
    val timestamp: String,
    val severity: String,
    val createdAt: String
)