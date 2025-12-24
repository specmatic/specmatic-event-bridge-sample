package io.specmatic.async.service

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.sqs.SqsClient
import aws.sdk.kotlin.services.sqs.model.DeleteMessageRequest
import aws.sdk.kotlin.services.sqs.model.Message
import aws.sdk.kotlin.services.sqs.model.ReceiveMessageRequest
import aws.smithy.kotlin.runtime.net.url.Url
import kotlinx.coroutines.delay
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class EventBridgeListenerService(
    private val queueUrl: String,
    private val awsEndpoint: String,
    private val region: String,
    private val pollIntervalMs: Long,
    private val onMessageReceived: suspend (Message) -> Unit
) : AutoCloseable {

    private var isRunning = false

    private val sqsClient = SqsClient {
        credentialsProvider = StaticCredentialsProvider {
            accessKeyId = "test"
            secretAccessKey = "test"
        }
        region = this@EventBridgeListenerService.region
        endpointUrl = Url.parse(this@EventBridgeListenerService.awsEndpoint)
    }

    suspend fun startListening() {
        isRunning = true
        logger.info { "Starting to listen for messages from SQS queue: $queueUrl" }

        while (isRunning) {
            try {
                receiveAndProcessMessages()
                delay(pollIntervalMs)
            } catch (e: Exception) {
                logger.error(e) { "Error while polling messages: ${e.message}" }
                delay(pollIntervalMs)
            }
        }
    }

    private suspend fun receiveAndProcessMessages() {
        val request = ReceiveMessageRequest {
            queueUrl = this@EventBridgeListenerService.queueUrl
            maxNumberOfMessages = 10
            waitTimeSeconds = 20
        }

        val response = sqsClient.receiveMessage(request)

        response.messages?.forEach { message ->
            try {
                logger.info { "Received message from SQS: ${message.messageId}" }
                logger.debug { "Message body: ${message.body}" }

                onMessageReceived(message)

                // Delete message from queue after successful processing
                deleteMessage(message.receiptHandle!!)
            } catch (e: Exception) {
                logger.error(e) { "Error processing message ${message.messageId}: ${e.message}" }
            }
        }
    }

    private suspend fun deleteMessage(receiptHandle: String) {
        val deleteRequest = DeleteMessageRequest {
            queueUrl = this@EventBridgeListenerService.queueUrl
            this.receiptHandle = receiptHandle
        }

        sqsClient.deleteMessage(deleteRequest)
        logger.debug { "Deleted message from queue" }
    }

    fun stop() {
        logger.info { "Stopping EventBridge listener" }
        isRunning = false
    }

    override fun close() {
        stop()
        sqsClient.close()
    }
}

