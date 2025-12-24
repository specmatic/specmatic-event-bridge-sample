package io.specmatic.async.config

import java.util.Properties

data class AppConfig(
    val awsRegion: String = System.getenv("AWS_REGION") ?: "us-east-1",
    val eventBridgeBusName: String = System.getenv("EVENT_BRIDGE_BUS_NAME") ?: "order-events-bus",
    val sqsQueueUrl: String = System.getenv("SQS_QUEUE_URL") ?: "http://localhost:4566/000000000000/order-events-queue",
    val awsEndpoint: String = System.getenv("AWS_ENDPOINT") ?: "http://localhost:4566",
    val kafkaBootstrapServers: String = System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "localhost:9092",
    val kafkaTopic: String = System.getenv("KAFKA_TOPIC") ?: "order-events",
    val pollIntervalMs: Long = System.getenv("POLL_INTERVAL_MS")?.toLong() ?: 5000L
) {
    fun getKafkaProducerProps(): Properties {
        return Properties().apply {
            put("bootstrap.servers", kafkaBootstrapServers)
            put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
            put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
            put("acks", "all")
            put("retries", 3)
            put("linger.ms", 1)
            put("buffer.memory", 33554432)
        }
    }
}
