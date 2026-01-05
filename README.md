# EventBridge to Kafka Bridge Application

This application listens to AWS EventBridge events (via SQS) and publishes them to Kafka topics based on event type:
- `place-order-event` → Published to `place-order` Kafka topic
- `cancel-order-event` → Published to `cancel-order` Kafka topic


## Architecture

The application consists of:
1. **EventBridge** (LocalStack) - Receives events from external sources
2. **SQS Queue** - EventBridge rules route events to this queue
3. **Application** - Polls SQS, processes events, and publishes to Kafka
4. **Kafka** (Confluent) - Stores the processed events

## Prerequisites

- Docker and Docker Compose
- Java 17 or higher
- Gradle (included via wrapper)


## Running Contract Tests using Specmatic Async

Run the contract tests using
```shell
./gradlew clean test
```

This will run the ContractTest written in `src/test/resources/ContractTest.kt`. 

The contract test uses of specmatic-async which makes use of the asyncapi specification located at `spec/order-events-async-api.yaml` to generate and run the tests.

## Running the Application

### ⚡ Quick Commands Using make

```bash
make check          # Check prerequisites
make setup          # Full automated setup
make build          # Build the application
make run            # Run the application
make troubleshoot   # Diagnose any issues
make help           # See all commands
```

### Quick Start

**1. Start Docker Desktop** (macOS/Windows) or Docker daemon (Linux)

**2. Run the setup script:**

```bash
chmod +x setup.sh
./setup.sh
```

This script will:
1. Check prerequisites (Docker, Java)
2. Start all services (LocalStack, Kafka, Zookeeper, Kafka UI)
3. Wait for services to be healthy
4. Create necessary AWS resources (EventBridge bus, SQS queue, rules)
5. Create the Kafka topic

### Manual Setup

If you prefer to set up manually or need to troubleshoot:

#### 1. Start Docker Services

```bash
chmod +x start.sh
./start.sh
```

Or directly:
```bash
docker compose up -d
```

This starts:
- LocalStack (EventBridge & SQS) on port 4566
- Kafka on port 9092
- Zookeeper on port 2181
- Kafka UI on port 8080

#### 2. Verify Services are Running

```bash
docker compose ps
```

All services should show as "healthy" after 1-2 minutes. If they don't:

```bash
chmod +x troubleshoot.sh
./troubleshoot.sh
```

### 2. Build the Application

```bash
./gradlew build
```

### 3. Run the Application

```bash
./gradlew run
```

### 4. Send Test Events

Make the script executable and run it:

```bash
chmod +x scripts/send-test-events.sh
./scripts/send-test-events.sh
```

### 5. Verify Messages in Kafka

Option 1: Using the consumer scripts

**Consume from both topics:**
```bash
chmod +x scripts/consume-kafka-messages.sh
./scripts/consume-kafka-messages.sh
```

**Consume only place-order events:**
```bash
chmod +x scripts/consume-place-order.sh
./scripts/consume-place-order.sh
```

**Consume only cancel-order events:**
```bash
chmod +x scripts/consume-cancel-order.sh
./scripts/consume-cancel-order.sh
```

Option 2: Using Kafka UI
Open http://localhost:8080 in your browser and check the `place-order` and `cancel-order` topics


## Cleanup

Stop and remove all containers:

```bash
docker compose down -v
```

