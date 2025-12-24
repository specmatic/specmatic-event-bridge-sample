package io.specmatic.async.processor

import aws.sdk.kotlin.services.sqs.model.Message
import io.specmatic.async.model.CancelOrderEvent
import io.specmatic.async.model.PlaceOrderEvent
import io.specmatic.async.service.KafkaProducerService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class EventProcessor(
    private val kafkaProducer: KafkaProducerService
) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    suspend fun processMessage(message: Message) {
        try {
            val body = message.body ?: run {
                logger.warn { "Received message with null body" }
                return
            }

            // Parse the EventBridge message (which is wrapped in SQS format)
            val sqsMessage = json.parseToJsonElement(body).jsonObject
            val eventBridgeMessage = sqsMessage["detail"]?.toString() ?: body

            // Extract event type
            val eventDetail = json.parseToJsonElement(eventBridgeMessage).jsonObject
            val eventType = eventDetail["eventType"]?.jsonPrimitive?.content

            logger.info { "Processing event type: $eventType" }

            when (eventType) {
                "place-order-event" -> {
                    val event = json.decodeFromString<PlaceOrderEvent>(eventBridgeMessage)
                    processPlaceOrderEvent(event)
                }
                "cancel-order-event" -> {
                    val event = json.decodeFromString<CancelOrderEvent>(eventBridgeMessage)
                    processCancelOrderEvent(event)
                }
                else -> {
                    logger.warn { "Unknown event type: $eventType" }
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error processing message: ${e.message}" }
            throw e
        }
    }

    private fun processPlaceOrderEvent(event: PlaceOrderEvent) {
        logger.info { "Processing PlaceOrderEvent for order: ${event.orderId}" }

        val message = Json.encodeToString(PlaceOrderEvent.serializer(), event)
        kafkaProducer.sendMessage("place-order", event.orderId, message)

        logger.info { "PlaceOrderEvent sent to Kafka topic 'place-order' for order: ${event.orderId}" }
    }

    private fun processCancelOrderEvent(event: CancelOrderEvent) {
        logger.info { "Processing CancelOrderEvent for order: ${event.orderId}" }

        val message = Json.encodeToString(CancelOrderEvent.serializer(), event)
        kafkaProducer.sendMessage("cancel-order", event.orderId, message)

        logger.info { "CancelOrderEvent sent to Kafka topic 'cancel-order' for order: ${event.orderId}" }
    }
}

