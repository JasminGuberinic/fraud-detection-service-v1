package com.solution.fraud_detection.consumer.service

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.solution.fraud_detection.common.model.FraudAlert
import com.solution.fraud_detection.consumer.mapper.toElk
import org.springframework.kafka.support.Acknowledgment
import java.time.format.DateTimeFormatter

/**
 * Service for consuming fraud alerts from Kafka
 * Implements manual acknowledgment and error handling
 */
@Service
class AlertConsumerService(
    private val objectMapper: ObjectMapper,
    private val alertStorageService: AlertStorageService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    @KafkaListener(
        topics = ["\${kafka.topic.fraud-alerts}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun consumeAlert(
        @Payload alertJson: String,
        @Header(KafkaHeaders.RECEIVED_KEY) key: String?,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment
    ) {
        try {
            logger.info("Received message from topic: $topic, partition: $partition, offset: $offset")

            val alert = objectMapper.readValue(alertJson, FraudAlert::class.java)
            processAlert(alert)

            // Save alert to Elasticsearch
            alertStorageService.saveAlert(alert.toElk())

            // Acknowledge successful processing
            acknowledgment.acknowledge()
            logger.info("Successfully processed and acknowledged alert: ${alert.id}")

        } catch (e: Exception) {
            logger.error("Error processing alert from topic: $topic, partition: $partition, offset: $offset", e)
            // In case of error, don't acknowledge - message will be redelivered
            // Depending on your requirements, you might want to implement dead letter queue handling here
            handleError(alertJson, e)
        }
    }

    private fun processAlert(alert: FraudAlert) {
        logger.info("""
            |🚨 Fraud Alert Detected 🚨
            |ID: ${alert.id}
            |Type: ${alert.type}
            |Transaction ID: ${alert.transactionId}
            |Time: ${alert.timestamp}
            |Description: ${alert.description}
            |""".trimMargin()
        )

        // - Save to database
        // - Send notifications
        // - Trigger other business processes
    }

    private fun handleError(alertJson: String, error: Exception) {
        // - Save to dead letter queue
        // - Send notification to support team
        // - Log to error monitoring system
        logger.error("Failed to process alert: $alertJson", error)
    }
}