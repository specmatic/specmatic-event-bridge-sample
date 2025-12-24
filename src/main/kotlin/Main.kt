package io.specmatic.async

import io.specmatic.async.config.AppConfig
import io.specmatic.async.processor.EventProcessor
import io.specmatic.async.service.EventBridgeListenerService
import io.specmatic.async.service.KafkaProducerService
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
private val logger = KotlinLogging.logger {}

fun main() = runBlocking {
    logger.info { "Starting EventBridge to Kafka Bridge Application" }

    val config = AppConfig()

    logger.info {
        """
        Configuration:
        - AWS Region: ${config.awsRegion}
        - AWS Endpoint: ${config.awsEndpoint}
        - EventBridge Bus: ${config.eventBridgeBusName}
        - SQS Queue URL: ${config.sqsQueueUrl}
        - Kafka Bootstrap Servers: ${config.kafkaBootstrapServers}
        - Kafka Topic: ${config.kafkaTopic}
        - Poll Interval: ${config.pollIntervalMs}ms
        """.trimIndent()
    }

    // Initialize Kafka Producer
    val kafkaProducer = KafkaProducerService(
        kafkaProps = config.getKafkaProducerProps()
    )

    // Initialize Event Processor
    val eventProcessor = EventProcessor(kafkaProducer)

    // Initialize EventBridge Listener
    val eventBridgeListener = EventBridgeListenerService(
        queueUrl = config.sqsQueueUrl,
        awsEndpoint = config.awsEndpoint,
        region = config.awsRegion,
        pollIntervalMs = config.pollIntervalMs,
        onMessageReceived = { message ->
            eventProcessor.processMessage(message)
        }
    )

    // Add shutdown hook
    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "Shutting down application..." }
        eventBridgeListener.close()
        kafkaProducer.close()
        logger.info { "Application shutdown complete" }
    })

    // Start listening
    try {
        eventBridgeListener.startListening()
    } catch (e: Exception) {
        logger.error(e) { "Application error: ${e.message}" }
        eventBridgeListener.close()
        kafkaProducer.close()
        throw e
    }
}