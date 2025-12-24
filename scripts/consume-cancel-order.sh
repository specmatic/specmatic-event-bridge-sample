#!/bin/bash

# Script to consume messages from cancel-order Kafka topic

echo "Consuming messages from Kafka topic: cancel-order"
echo "Press Ctrl+C to stop"
echo "========================================"

docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic cancel-order \
  --from-beginning \
  --property print.key=true \
  --property key.separator=" | "

