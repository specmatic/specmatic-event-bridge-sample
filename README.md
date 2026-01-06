# EventBridge to Kafka Bridge Application

This application demonstrates an event-driven architecture that bridges AWS EventBridge with Apache Kafka. The flow works as follows:

1. External services publish events to AWS EventBridge
2. EventBridge rules route events to an SQS queue based on event type
3. This application polls the SQS queue for messages
4. Events are processed and published to corresponding Kafka topics:
   - `place-order-event` → `place-order` topic
   - `cancel-order-event` → `cancel-order` topic

## Prerequisites

- Docker and Docker Compose
- Java 17 or higher
- Gradle (included via wrapper)

## Running Contract Tests

The project uses [Specmatic](https://specmatic.io/) to run contract tests against the AsyncAPI specification.

```bash
./gradlew clean test
```

This runs contract tests defined in `src/test/kotlin/ContractTest.kt` which:
- Starts LocalStack (EventBridge/SQS) and Kafka using docker-compose
- Launches the application
- Runs Specmatic to validate the application against `spec/order-events-async-api.yaml`

## Building the Application

```bash
./gradlew build
```

## Running the Application

### 1. Start Infrastructure

Start LocalStack, Kafka, and Zookeeper:

```bash
docker compose up -d
```

### 2. Start the Application

```bash
./gradlew run
```

### 3. Send Test Events

```bash
./scripts/send-test-events.sh
```

### 4. Verify Events in Kafka

View messages using Kafka UI at http://localhost:8080


## Cleanup

```bash
docker compose down -v
```

