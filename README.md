# Fraud Detection Service

Real-time transaction monitoring and fraud detection system built with Spring Boot and Kafka Streams.

## Overview

This service processes financial transactions in real-time to detect potentially fraudulent activities using stream processing and machine learning techniques.

## Tech Stack

- **Spring Boot 3.4.2**: Core framework
- **Kotlin 1.9.25**: Programming language
- **Apache Kafka**: Event streaming platform
- **Kafka Streams**: Stream processing library
- **gRPC**: High-performance RPC framework
- **Elasticsearch**: Search and analytics engine
- **Protocol Buffers**: Data serialization

## Key Features

- Real-time transaction processing
- Stream-based fraud detection
- Multiple fraud detection patterns:
    - High-amount transactions
    - Velocity checks
    - Geographic anomalies
- gRPC and REST API endpoints
- Kafka-based event streaming
- Elasticsearch integration for transaction indexing

## Architecture

The service follows a stream processing architecture:

1. Transaction ingestion via REST/gRPC
2. Kafka-based event streaming
3. Real-time fraud detection using Kafka Streams
4. Alert generation for suspicious activities
5. Transaction indexing in Elasticsearch

## API Endpoints

### REST API
- POST `/api/transactions`: Submit new transaction
- GET `/api/transactions/{id}`: Retrieve transaction details

### gRPC Service
- `SendTransaction`: Submit new transaction via gRPC
- `GetTransaction`: Retrieve transaction details via gRPC

## Configuration

The service supports configuration via:
- Application properties
- Environment variables
- External configuration server

## Getting Started

1. Prerequisites:
    - JDK 17
    - Apache Kafka
    - Elasticsearch

## Keywords
fraud detection, real-time processing, kafka streams, spring boot, grpc, elasticsearch, financial transactions, stream processing, kotlin
