package com.solution.fraud_detection.common.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

/**
 * Konfiguracija za automatsko kreiranje Kafka topica
 * Topici će biti kreirani pri pokretanju aplikacije ako već ne postoje
 */
@Configuration
class KafkaTopicConfig {

    @Value("\${kafka.topic.transactions}")
    private lateinit var transactionsTopic: String

    /**
     * Kreira transactions topic ako ne postoji
     * @return NewTopic objekat koji Spring koristi za kreiranje topica
     */
    @Bean
    fun transactionsTopic(): NewTopic {
        return TopicBuilder.name(transactionsTopic)
            .partitions(1)         // Broj particija (za početak 1)
            .replicas(1)           // Broj replika (za development 1)
            .build()
    }
}