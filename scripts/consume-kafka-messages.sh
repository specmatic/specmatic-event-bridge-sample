#!/bin/bash

# Script to consume messages from Kafka topics
# Usage: ./consume-kafka-messages.sh [topic]
# If no topic is specified, it will consume from both place-order and cancel-order topics

TOPIC=${1:-"place-order,cancel-order"}

echo "Consuming messages from Kafka topic(s): $TOPIC"
echo "Press Ctrl+C to stop"
echo "========================================"

docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic "$TOPIC" \
  --from-beginning \
  --property print.key=true \
  --property key.separator=" | "

