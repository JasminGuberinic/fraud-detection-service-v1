package com.solution.fraud_detection.producer.service

import com.solution.fraud_detection.common.model.Transaction
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Value
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.random.Random


//# Generisanje random transakcija
//curl -X POST http://localhost:8081/api/transactions/generate/5
//
//# Slanje pojedinačne transakcije
//curl -X POST http://localhost:8081/api/transactions/send \
//-H "Content-Type: application/json" \
//-d '{
//"id": "TRX-123",
//"accountId": "ACC-456",
//"amount": 1000.0,
//"timestamp": "2024-02-11 12:00:00",
//"merchantId": "MERCH-789",
//"location": "New York"
//}'

@Service
class TransactionProducerService(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${kafka.topic.transactions}")
    private lateinit var transactionTopic: String

    fun sendTransaction(transaction: Transaction) {
        try {
            val transactionJson = objectMapper.writeValueAsString(transaction)
            logger.info("Sending transaction: $transactionJson")

            kafkaTemplate.send(transactionTopic, transaction.id, transactionJson)
                .whenComplete { result, ex ->
                    when {
                        ex != null -> logger.error("Failed to send transaction: ${transaction.id}", ex)
                        else -> logger.info("Successfully sent transaction: ${transaction.id}, offset: ${result.recordMetadata.offset()}")
                    }
                }
        } catch (e: Exception) {
            logger.error("Error while sending transaction", e)
            throw e
        }
    }
}