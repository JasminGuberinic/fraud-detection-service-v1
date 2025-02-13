package com.solution.fraud_detection.processor.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfig {

    @Value("\${kafka.topic.transactions}")
    private lateinit var transactionsTopic: String

    @Value("\${kafka.topic.fraud-alerts}")
    private lateinit var fraudAlertsTopic: String

    @Bean
    fun transactionsTopic(): NewTopic {
        return TopicBuilder.name(transactionsTopic)
            .partitions(1)
            .replicas(1)
            .build()
    }

    @Bean
    fun fraudAlertsTopic(): NewTopic {
        return TopicBuilder.name(fraudAlertsTopic)
            .partitions(1)
            .replicas(1)
            .build()
    }
}