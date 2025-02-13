package com.solution.fraud_detection.common.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime


data class Transaction(
    @JsonProperty("id")
    val id: String,

    @JsonProperty("accountId")
    val accountId: String,

    @JsonProperty("amount")
    val amount: Double,

    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val timestamp: LocalDateTime,

    @JsonProperty("merchantId")
    val merchantId: String,

    @JsonProperty("location")
    val location: String
) {
    override fun toString(): String =
        """{"id":"$id","accountId":"$accountId","amount":$amount,"timestamp":"${timestamp.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}","merchantId":"$merchantId","location":"$location"}"""
}