package com.solution.fraud_detection.producer.controller

import com.solution.fraud_detection.common.model.Transaction
import com.solution.fraud_detection.producer.service.TransactionProducerService
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/transactions")
class TransactionController(
    private val transactionProducerService: TransactionProducerService
) {
    @PostMapping("/send")
    fun sendTransaction(@RequestBody transaction: Transaction): ResponseEntity<String> {
        return try {
            transactionProducerService.sendTransaction(transaction)
            ResponseEntity.ok("Transaction sent successfully")
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body("Failed to send transaction: ${e.message}")
        }
    }
}