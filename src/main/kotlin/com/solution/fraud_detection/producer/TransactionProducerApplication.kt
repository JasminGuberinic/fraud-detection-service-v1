package com.solution.fraud_detection.producer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.PropertySources

@SpringBootApplication
@PropertySources(
    PropertySource("classpath:application.properties"),
    PropertySource("classpath:application-producer.properties")
)
class TransactionProducerApplication

fun main(args: Array<String>) {
    runApplication<TransactionProducerApplication>(*args)
}