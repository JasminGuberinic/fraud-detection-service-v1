package com.solution.fraud_detection.processor.stream

import com.fasterxml.jackson.databind.ObjectMapper
import com.solution.fraud_detection.common.model.Transaction
import com.solution.fraud_detection.common.model.FraudAlert
import com.solution.fraud_detection.common.model.FraudAlertType
import com.solution.fraud_detection.processor.service.FraudDetectionService
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

/**
 * Kafka Streams processor for real-time fraud detection.
 * Implements multiple fraud detection strategies:
 * 1. Basic fraud detection (amount and location-based)
 * 2. Multiple transactions detection within a time window
 * 3. Suspicious location pattern detection
 */
@Configuration
class TransactionStreamProcessor(
    private val fraudDetectionService: FraudDetectionService,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${kafka.topic.transactions}")
    private lateinit var transactionsTopic: String

    @Value("\${kafka.topic.fraud-alerts}")
    private lateinit var fraudAlertsTopic: String

    @Value("\${fraud.detection.time-window-minutes:5}")
    private var timeWindowMinutes: Long = 1

    @Value("\${fraud.detection.transaction-count-threshold:3}")
    private var transactionCountThreshold: Long = 3

    /**
     * Custom Serde for Transaction objects.
     * Handles serialization/deserialization between Transaction objects and byte arrays.
     */
    private fun getTransactionSerde(): Serde<Transaction> = Serdes.serdeFrom(
        { _: String?, data: Transaction? ->
            data?.let { objectMapper.writeValueAsString(it).toByteArray() }
        },
        { _: String?, data: ByteArray? ->
            data?.let { objectMapper.readValue(it, Transaction::class.java) }
        }
    )

    /**
     * Configuration for standard String key/value pairs output.
     */
    private fun getStringProduced(): Produced<String, String> =
        Produced.with(Serdes.String(), Serdes.String())

    /**
     * Configuration for windowed output with String values.
     */
    private fun getWindowedStringProduced(): Produced<Windowed<String>, String> =
        Produced.with(WindowedSerdes.timeWindowedSerdeFrom(String::class.java), Serdes.String())

    /**
     * Custom Serde for Set<String> objects.
     * Handles serialization/deserialization of location sets.
     */
    private fun getLocationSetSerde(): Serde<Set<String>> = Serdes.serdeFrom(
        { _: String?, data: Set<String>? ->
            data?.let { objectMapper.writeValueAsString(it).toByteArray() }
        },
        { _: String?, data: ByteArray? ->
            data?.let {
                objectMapper.readValue(it,
                    objectMapper.typeFactory.constructCollectionType(Set::class.java, String::class.java)
                )
            }
        }
    )

    @Bean
    fun processTransactions(streamsBuilder: StreamsBuilder): KStream<String, String> {
        // Create base transaction stream with parsing and validation
        val transactionStream = createBaseTransactionStream(streamsBuilder)

        // Apply different fraud detection strategies
        detectBasicFraud(transactionStream)
        detectMultipleTransactions(transactionStream)
        detectLocationPatterns(transactionStream)

        return transactionStream.mapValues { value: Transaction? ->
            value?.let { objectMapper.writeValueAsString(it) } ?: ""
        }
    }

    /**
     * Creates the base transaction stream with JSON parsing and validation.
     */
    private fun createBaseTransactionStream(streamsBuilder: StreamsBuilder): KStream<String, Transaction?> =
        streamsBuilder
            .stream<String, String>(transactionsTopic)
            .peek { key, _ -> logger.info("Processing transaction - Key: $key") }
            .mapValues { value ->
                try {
                    objectMapper.readValue(value, Transaction::class.java)
                } catch (e: Exception) {
                    logger.error("Error parsing transaction: $value", e)
                    null
                }
            }
            .filter { _, transaction -> transaction != null }

    /**
     * Detects basic fraud patterns using FraudDetectionService.
     * Handles amount-based and single-location-based fraud detection.
     */
    private fun detectBasicFraud(transactionStream: KStream<String, Transaction?>) {
        transactionStream
            .mapValues { transaction ->
                val alert = fraudDetectionService.analyzeTransaction(transaction!!)
                alert?.let { objectMapper.writeValueAsString(it) }
            }
            .filter { _, alert -> alert != null }
            .to(fraudAlertsTopic, getStringProduced())
    }

    /**
     * Detects multiple transactions from the same account within a time window.
     * Generates alerts when transaction count exceeds the configured threshold.
     */
    private fun detectMultipleTransactions(transactionStream: KStream<String, Transaction?>) {
        transactionStream
            .groupBy(
                { _, transaction -> transaction!!.accountId },
                Grouped.with(Serdes.String(), getTransactionSerde())
            )
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(timeWindowMinutes)))
            .count()
            .toStream()
            .filter { _, count -> count >= transactionCountThreshold }
            .mapValues { key, count ->
                createMultipleTransactionsAlert(key.key(), count)
            }
            .to(fraudAlertsTopic, getWindowedStringProduced())
    }

    /**
     * Detects suspicious location patterns by tracking unique transaction locations
     * for each account within a time window.
     */
    private fun detectLocationPatterns(transactionStream: KStream<String, Transaction?>) {
        transactionStream
            .groupBy(
                { _, transaction -> transaction!!.accountId },
                Grouped.with(Serdes.String(), getTransactionSerde())
            )
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(timeWindowMinutes)))
            .aggregate(
                { mutableSetOf<String>() },  // Initial value is mutable set
                { _, transaction, locations ->
                    locations.toMutableSet().apply {  // Convert to mutable, apply changes
                        add(transaction!!.location)
                    }
                },
                Materialized.with(Serdes.String(), getLocationSetSerde())
            )
            .toStream()
            .filter { _, locations -> locations.size >= 3 }
            .mapValues { key, locations ->
                createLocationPatternAlert(key.key(), locations)
            }
            .to(fraudAlertsTopic, getWindowedStringProduced())
    }

    /**
     * Creates an alert for multiple transactions detection.
     */
    private fun createMultipleTransactionsAlert(accountId: String, count: Long): String {
        val alert = FraudAlert(
            id = UUID.randomUUID().toString(),
            transactionId = "MULTIPLE-$accountId",
            type = FraudAlertType.MULTIPLE_TRANSACTIONS,
            description = "Account $accountId made $count transactions in $timeWindowMinutes minutes",
            timestamp = LocalDateTime.now(),
            accountId = accountId
        )
        return objectMapper.writeValueAsString(alert)
    }

    /**
     * Creates an alert for suspicious location patterns.
     */
    private fun createLocationPatternAlert(accountId: String, locations: Set<String>): String {
        val alert = FraudAlert(
            id = UUID.randomUUID().toString(),
            transactionId = "LOCATION-$accountId",
            type = FraudAlertType.SUSPICIOUS_LOCATION,
            description = "Multiple locations detected: ${locations.joinToString()}",
            timestamp = LocalDateTime.now(),
            accountId = accountId
        )
        return objectMapper.writeValueAsString(alert)
    }
}