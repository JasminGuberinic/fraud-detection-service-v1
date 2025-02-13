package com.solution.fraud_detection.consumer.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Duration
import java.time.format.DateTimeFormatter
import org.slf4j.LoggerFactory

@Configuration
@EnableElasticsearchRepositories(basePackages = ["com.solution.fraud_detection.consumer.repository"])
class ElasticsearchConfig : ElasticsearchConfiguration() {

    override fun clientConfiguration(): ClientConfiguration {
        return ClientConfiguration.builder()
            .connectedTo("localhost:9200")
            .withConnectTimeout(Duration.ofSeconds(5))
            .withSocketTimeout(Duration.ofSeconds(3))
            .build()
    }

    @Bean
    override fun elasticsearchCustomConversions(): ElasticsearchCustomConversions {
        return ElasticsearchCustomConversions(
            listOf(
                DateToLocalDateTimeConverter(),
                LocalDateTimeToDateConverter()
            )
        )
    }

    @ReadingConverter
    private class DateToLocalDateTimeConverter : Converter<String, LocalDateTime> {
        override fun convert(source: String): LocalDateTime? {
            return try {
                when {
                    source.contains("T") -> LocalDateTime.parse(source)
                    source.contains(".") -> LocalDateTime.parse(source.substring(0, source.indexOf(".")))
                    else -> LocalDate.parse(source).atStartOfDay()
                }
            } catch (e: Exception) {
                logger.warn("Failed to parse date: $source", e)
                null
            }
        }

        companion object {
            private val logger = LoggerFactory.getLogger(DateToLocalDateTimeConverter::class.java)
        }
    }

    @WritingConverter
    private class LocalDateTimeToDateConverter : Converter<LocalDateTime, String> {
        override fun convert(source: LocalDateTime): String {
            return source.format(DateTimeFormatter.ISO_DATE_TIME)
        }
    }
}