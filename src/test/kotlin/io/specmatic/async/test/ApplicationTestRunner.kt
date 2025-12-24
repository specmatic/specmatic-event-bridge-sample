package io.specmatic.async.test

import io.specmatic.async.config.AppConfig
import io.specmatic.async.processor.EventProcessor
import io.specmatic.async.service.EventBridgeListenerService
import io.specmatic.async.service.KafkaProducerService
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Manages the lifecycle of the EventBridge to Kafka Bridge application for testing.
 */
class ApplicationTestRunner(
    private val awsEndpoint: String = "http://localhost:4566",
    private val sqsQueueUrl: String = "http://localhost:4566/000000000000/order-events-queue",
    private val kafkaBootstrapServers: String = "localhost:9092",
    private val awsRegion: String = "us-east-1",
    private val pollIntervalMs: Long = 5000L
) : AutoCloseable {

    private lateinit var appThread: Thread
    private lateinit var eventBridgeListener: EventBridgeListenerService
    private lateinit var kafkaProducer: KafkaProducerService

    fun start() {
        println("Starting EventBridge to Kafka Bridge application...")

        val config = AppConfig(
            awsRegion = awsRegion,
            awsEndpoint = awsEndpoint,
            sqsQueueUrl = sqsQueueUrl,
            kafkaBootstrapServers = kafkaBootstrapServers,
            pollIntervalMs = pollIntervalMs
        )

        println("Configuration:")
        println("  AWS Endpoint: ${config.awsEndpoint}")
        println("  SQS Queue URL: ${config.sqsQueueUrl}")
        println("  Kafka Bootstrap Servers: ${config.kafkaBootstrapServers}")

        // Initialize Kafka Producer
        kafkaProducer = KafkaProducerService(
            kafkaProps = config.getKafkaProducerProps()
        )

        // Initialize Event Processor
        val eventProcessor = EventProcessor(kafkaProducer)

        // Initialize EventBridge Listener
        eventBridgeListener = EventBridgeListenerService(
            queueUrl = config.sqsQueueUrl,
            awsEndpoint = config.awsEndpoint,
            region = config.awsRegion,
            pollIntervalMs = config.pollIntervalMs,
            onMessageReceived = { message ->
                eventProcessor.processMessage(message)
            }
        )

        // Start the application in a background thread
        appThread = Thread {
            runBlocking {
                try {
                    eventBridgeListener.startListening()
                } catch (e: Exception) {
                    println("Application error: ${e.message}")
                    e.printStackTrace()
                }
            }
        }.apply {
            isDaemon = true
            start()
        }

        // Wait for the application to start
        Thread.sleep(5000)

        println("Application started successfully")
    }

    override fun close() {
        println("Stopping application gracefully...")

        if (::eventBridgeListener.isInitialized) {
            eventBridgeListener.close()
        }

        if (::kafkaProducer.isInitialized) {
            kafkaProducer.close()
        }

        if (::appThread.isInitialized && appThread.isAlive) {
            appThread.join(5000)
        }

        println("Application stopped")
    }

    companion object {
        /**
         * Initializes AWS resources (EventBridge, SQS) by running the init script.
         */
        fun initializeAwsResources() {
            println("Initializing AWS resources (EventBridge, SQS)...")

            val initScript = File("localstack-init/01-init-resources.sh")
            if (initScript.exists()) {
                val processBuilder = ProcessBuilder(
                    "docker", "exec",
                    "-e", "AWS_ACCESS_KEY_ID=test",
                    "-e", "AWS_SECRET_ACCESS_KEY=test",
                    "-e", "AWS_DEFAULT_REGION=us-east-1",
                    "localstack", "bash", "/etc/localstack/init/ready.d/01-init-resources.sh"
                )
                val process = processBuilder.start()
                val exitCode = process.waitFor()

                if (exitCode == 0) {
                    println("AWS resources initialized successfully")
                } else {
                    println("Warning: AWS resources initialization returned exit code $exitCode")
                }
            } else {
                println("Warning: Init script not found, AWS resources may not be initialized")
            }

            Thread.sleep(3000)
        }
    }
}

