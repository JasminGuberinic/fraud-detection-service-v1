package com.solution.fraud_detection.common.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class FraudAlert(
    @JsonProperty("id")
    val id: String,

    @JsonProperty("accountId")
    val accountId: String,

    @JsonProperty("transactionId")
    val transactionId: String,

    @JsonProperty("type")
    val type: FraudAlertType,

    @JsonProperty("description")
    val description: String,

    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val timestamp: LocalDateTime
) {
    override fun toString(): String =
        """{"id":"$id","accountId":"$accountId","transactionId":"$transactionId","type":"${type.toString()}","description":"$description","timestamp":"${timestamp.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}"}"""
}