package com.solution.fraud_detection.consumer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.PropertySources

@SpringBootApplication
@PropertySources(
    PropertySource("classpath:application.properties"),
    PropertySource("classpath:application-consumer.properties")
)
class AlertConsumerApplication

fun main(args: Array<String>) {
    runApplication<AlertConsumerApplication>(*args)
}