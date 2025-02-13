package com.solution.fraud_detection.consumer.controller


import com.solution.fraud_detection.consumer.mapper.toResponse
import com.solution.fraud_detection.consumer.service.AlertStorageService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/alerts")
class AlertController(
    private val alertStorageService: AlertStorageService
) {
    @GetMapping("/{id}")
    fun getAlertById(@PathVariable id: String): ResponseEntity<FraudAlertResponse> {
        return alertStorageService.findById(id)?.let {
            ResponseEntity.ok(it.toResponse())
        } ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/account/{accountId}")
    fun getAlertsByAccount(@PathVariable accountId: String): ResponseEntity<List<FraudAlertResponse>> {
        return ResponseEntity.ok(alertStorageService.findByAccountId(accountId).map { it.toResponse() })
    }

    @GetMapping("/search")
    fun getAlertsByTimeRange(
        @RequestParam startTime: LocalDateTime,
        @RequestParam endTime: LocalDateTime
    ): ResponseEntity<List<FraudAlertResponse>> {
        return ResponseEntity.ok(alertStorageService.findByTimeRange(startTime, endTime).map { it.toResponse() })
    }

    @GetMapping
    fun getAllAlerts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<FraudAlertResponse>> {
        val pageable = PageRequest.of(page, size)
        return ResponseEntity.ok(alertStorageService.findAllPaged(pageable).map { it.toResponse() })
    }
}