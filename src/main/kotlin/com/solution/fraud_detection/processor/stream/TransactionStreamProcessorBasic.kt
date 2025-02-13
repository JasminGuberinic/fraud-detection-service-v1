//package com.solution.fraud_detection.processor.stream
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.solution.fraud_detection.common.model.Transaction
//import com.solution.fraud_detection.processor.service.FraudDetectionService
//import org.apache.kafka.streams.StreamsBuilder
//import org.apache.kafka.streams.kstream.KStream
//import org.slf4j.LoggerFactory
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.kafka.annotation.EnableKafkaStreams
//
//@Configuration
//@EnableKafkaStreams
//class TransactionStreamProcessorBasic(
//    private val fraudDetectionService: FraudDetectionService,
//    private val objectMapper: ObjectMapper
//) {
//    private val logger = LoggerFactory.getLogger(javaClass)
//
//    @Value("\${kafka.topic.transactions}")
//    private lateinit var transactionsTopic: String
//
//    @Bean
//    fun processTransactions(streamsBuilder: StreamsBuilder): KStream<String, String> {
//        return streamsBuilder
//            .stream<String, String>(transactionsTopic)
//            .peek { key, value ->
//                logger.info("Processing transaction - Key: $key")
//            }
//            .mapValues { value ->
//                try {
//                    objectMapper.readValue(value, Transaction::class.java)
//                } catch (e: Exception) {
//                    logger.error("Error parsing transaction: $value", e)
//                    null
//                }
//            }
//            .filter { _, transaction -> transaction != null }
//            .peek { _, transaction ->
//                try {
//                    fraudDetectionService.analyzeTransaction(transaction!!)
//                } catch (e: Exception) {
//                    logger.error("Error analyzing transaction: ${transaction?.id}", e)
//                }
//            }
//            .mapValues { value -> value.toString() }
//    }
//}