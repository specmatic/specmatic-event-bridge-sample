#!/bin/bash

# Script to consume messages from place-order Kafka topic

echo "Consuming messages from Kafka topic: place-order"
echo "Press Ctrl+C to stop"
echo "========================================"

docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic place-order \
  --from-beginning \
  --property print.key=true \
  --property key.separator=" | "

