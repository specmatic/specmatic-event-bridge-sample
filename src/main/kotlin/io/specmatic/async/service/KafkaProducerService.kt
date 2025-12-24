package io.specmatic.async.service

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import mu.KotlinLogging
import java.util.Properties
import java.util.concurrent.Future

private val logger = KotlinLogging.logger {}

class KafkaProducerService(
    private val kafkaProps: Properties
) : AutoCloseable {

    private val producer: KafkaProducer<String, String> = KafkaProducer(kafkaProps)

    fun sendMessage(topic: String, key: String, message: String): Future<RecordMetadata> {
        logger.info { "Sending message to Kafka topic '$topic' with key: $key" }

        val record = ProducerRecord(topic, key, message)

        return producer.send(record) { metadata, exception ->
            if (exception != null) {
                logger.error(exception) { "Error sending message to Kafka: ${exception.message}" }
            } else {
                logger.info {
                    "Message sent successfully to topic: ${metadata.topic()}, " +
                    "partition: ${metadata.partition()}, " +
                    "offset: ${metadata.offset()}"
                }
            }
        }
    }

    override fun close() {
        logger.info { "Closing Kafka producer" }
        producer.flush()
        producer.close()
    }
}

