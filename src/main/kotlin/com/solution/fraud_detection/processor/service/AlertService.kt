package com.solution.fraud_detection.processor.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.solution.fraud_detection.common.model.FraudAlert
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value

/**
 * Servis za slanje i upravljanje alertima
 * Trenutno šalje alerte na Kafka topic i loguje ih
 */
@Service
class AlertService(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${kafka.topic.fraud-alerts}")
    private lateinit var fraudAlertsTopic: String

    fun sendAlert(alert: FraudAlert) {
        try {
            val alertJson = objectMapper.writeValueAsString(alert)
            kafkaTemplate.send(fraudAlertsTopic, alert.id, alertJson)
                .whenComplete { result, ex ->
                    when {
                        ex != null -> logger.error("Failed to send alert: ${alert.id}", ex)
                        else -> logger.info("Successfully sent alert: ${alert.id}")
                    }
                }
        } catch (e: Exception) {
            logger.error("Error while sending alert", e)
            throw e
        }
    }
}