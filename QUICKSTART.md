# Quick Start Guide

## 🚀 Quick Start (All-in-one)

```bash
# 1. Start all services (LocalStack, Kafka, Zookeeper)
./setup.sh

# 2. Build and run the application
./gradlew run
```

## 📝 Step-by-Step Guide

### 1. Start Infrastructure

```bash
# Start LocalStack and Kafka
docker compose up -d

# Verify services are running
docker compose ps
```

### 2. Build the Application

```bash
./gradlew build
```

### 3. Run the Application

```bash
./gradlew run
```

Or run the JAR directly:

```bash
./gradlew jar
java -jar build/libs/specmatic-event-bridge-sample-1.0-SNAPSHOT.jar
```

### 4. Test the Application

In a new terminal, send test events:

```bash
./scripts/send-test-events.sh
```

### 5. Verify Messages in Kafka

Option 1 - Using the console consumer script:
```bash
./scripts/consume-kafka-messages.sh
```

Option 2 - Using Kafka UI:
Open http://localhost:8080 in your browser and navigate to Topics → order-events

## 📊 Monitoring

### Check Application Logs
The application logs will show:
- Configuration on startup
- Messages received from SQS
- Events processed
- Messages sent to Kafka

### View SQS Queue
```bash
awslocal sqs receive-message \
  --queue-url http://localhost:4566/000000000000/order-events-queue \
  --endpoint-url http://localhost:4566
```

### List EventBridge Rules
```bash
awslocal events list-rules \
  --event-bus-name order-events-bus \
  --endpoint-url http://localhost:4566
```

### Check Kafka Topics
```bash
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
```

### Describe Kafka Topic
```bash
docker exec kafka kafka-topics \
  --describe \
  --topic order-events \
  --bootstrap-server localhost:9092
```

## 🧪 Manual Testing

### Send a PlaceOrderEvent

```bash
awslocal events put-events \
  --entries '[
    {
      "Source": "order-service",
      "DetailType": "place-order-event",
      "Detail": "{\"eventType\":\"place-order-event\",\"timestamp\":\"2025-12-24T12:00:00Z\",\"orderId\":\"ORD-999\",\"customerId\":\"CUST-999\",\"items\":[{\"productId\":\"PROD-A\",\"quantity\":5,\"price\":99.99}],\"totalAmount\":499.95}",
      "EventBusName": "order-events-bus"
    }
  ]' \
  --endpoint-url=http://localhost:4566
```

### Send a CancelOrderEvent

```bash
awslocal events put-events \
  --entries '[
    {
      "Source": "order-service",
      "DetailType": "cancel-order-event",
      "Detail": "{\"eventType\":\"cancel-order-event\",\"timestamp\":\"2025-12-24T12:05:00Z\",\"orderId\":\"ORD-999\",\"customerId\":\"CUST-999\",\"reason\":\"Out of stock\"}",
      "EventBusName": "order-events-bus"
    }
  ]' \
  --endpoint-url=http://localhost:4566
```

## 🛑 Cleanup

```bash
# Stop the application (Ctrl+C)

# Stop and remove all containers
docker compose down -v

# Clean build artifacts
./gradlew clean
```

## 🔧 Troubleshooting

### Application won't start
1. Check if all services are running: `docker compose ps`
2. Check LocalStack logs: `docker logs localstack`
3. Check Kafka logs: `docker logs kafka`

### No messages in Kafka
1. Verify the application is running
2. Check application logs for errors
3. Verify SQS queue has messages: `awslocal sqs get-queue-attributes --queue-url http://localhost:4566/000000000000/order-events-queue --attribute-names All`

### Port conflicts
If ports 4566, 9092, 8080, or 2181 are already in use:
1. Stop the conflicting services
2. Or modify `docker-compose.yml` to use different ports

## ⚙️ Configuration

Environment variables can be set before running the application:

```bash
export AWS_REGION=us-west-2
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export KAFKA_TOPIC=my-custom-topic
./gradlew run
```

See `AppConfig.kt` for all available configuration options.

