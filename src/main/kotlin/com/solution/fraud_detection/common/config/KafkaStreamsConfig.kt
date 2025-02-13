package com.solution.fraud_detection.common.config

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafkaStreams
import org.springframework.kafka.config.KafkaStreamsConfiguration
import java.util.*

/**
 * Osnovna konfiguracija za Kafka Streams
 * @EnableKafkaStreams - Omogućava Kafka Streams funkcionalnosti u Spring aplikaciji
 */
@Configuration
@EnableKafkaStreams
class KafkaStreamsConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.streams.application-id}")
    private lateinit var applicationId: String

    /**
     * Kreira defaultni Kafka Streams konfiguracijski bean
     * VAŽNO: Ime metode/bean-a mora biti 'defaultKafkaStreamsConfig'
     */
    @Bean(name = ["defaultKafkaStreamsConfig"])
    fun defaultKafkaStreamsConfig(): KafkaStreamsConfiguration {
        val props = mapOf(
            // Osnovne konfiguracije
            StreamsConfig.APPLICATION_ID_CONFIG to applicationId,
            StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,

            // Default serdes konfiguracije
            StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String()::class.java.name,
            StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to Serdes.String()::class.java.name,

            // Dodatne konfiguracije za procesiranje
            StreamsConfig.PROCESSING_GUARANTEE_CONFIG to StreamsConfig.EXACTLY_ONCE_V2
        )

        return KafkaStreamsConfiguration(props)
    }
}