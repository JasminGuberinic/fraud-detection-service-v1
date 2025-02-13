package com.solution.fraud_detection.producer.grpc

import com.solution.fraud_detection.common.model.Transaction
import com.solution.fraud_detection.producer.service.TransactionProducerService
import io.grpc.Status
import net.devh.boot.grpc.server.service.GrpcService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * gRPC service implementation for handling transactions
 * This service provides methods for processing transaction requests via gRPC
 */
@GrpcService
class TransactionGrpcService(
    private val transactionProducerService: TransactionProducerService
) : TransactionServiceGrpc.TransactionServiceImplBase() {

    /**
     * Handles incoming transaction requests
     * Converts gRPC TransactionProto to domain Transaction and processes it
     */
    override fun sendTransaction(
        request: TransactionProto,
        responseObserver: io.grpc.stub.StreamObserver<TransactionResponse>
    ) {
        try {
            // Convert Proto message to domain Transaction
            val transaction = Transaction(
                id = request.id,
                accountId = request.accountId,
                amount = request.amount,
                timestamp = LocalDateTime.parse(
                    request.timestamp,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                ),
                merchantId = request.merchantId,
                location = request.location
            )

            // Process transaction using existing service
            transactionProducerService.sendTransaction(transaction)

            // Send successful response
            val response = TransactionResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Transaction processed successfully")
                .build()

            responseObserver.onNext(response)
            responseObserver.onCompleted()

        } catch (e: Exception) {
            // Handle errors
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Error processing transaction: ${e.message}")
                    .asRuntimeException()
            )
        }
    }
}