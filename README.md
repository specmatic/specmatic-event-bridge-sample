# EventBridge to Kafka Bridge Application

> 🚀 **Quick Start**: Run `./help.sh` or see [GETTING_STARTED.md](GETTING_STARTED.md)

This application listens to AWS EventBridge events (via SQS) and publishes them to Kafka topics based on event type:
- `place-order-event` → Published to `place-order` Kafka topic
- `cancel-order-event` → Published to `cancel-order` Kafka topic

## 📖 Documentation

- **[GETTING_STARTED.md](GETTING_STARTED.md)** - Quick reference guide
- **[SETUP_GUIDE.md](SETUP_GUIDE.md)** - Detailed setup & troubleshooting
- **README.md** (this file) - Architecture and configuration

## ⚡ Quick Commands

```bash
make check          # Check prerequisites
make setup          # Full automated setup
make build          # Build the application
make run            # Run the application
make troubleshoot   # Diagnose any issues
make help           # See all commands
```

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

**⚠️ IMPORTANT: Before starting, make sure Docker Desktop is running!**

### Check Prerequisites

Run this script to verify all prerequisites are met:

```bash
chmod +x check-prerequisites.sh
./check-prerequisites.sh
```

This will check:
- Docker installation and daemon status
- Docker Compose availability
- Java version
- Required ports availability

## Setup and Running

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

Or build a JAR and run it:

```bash
./gradlew jar
java -jar build/libs/specmatic-event-bridge-sample-1.0-SNAPSHOT.jar
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

## Configuration

The application can be configured via environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `AWS_REGION` | `us-east-1` | AWS region |
| `AWS_ENDPOINT` | `http://localhost:4566` | LocalStack endpoint |
| `EVENT_BRIDGE_BUS_NAME` | `order-events-bus` | EventBridge bus name |
| `SQS_QUEUE_URL` | `http://localhost:4566/000000000000/order-events-queue` | SQS queue URL |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka bootstrap servers |
| `KAFKA_TOPIC` | `order-events` | Kafka topic name |
| `POLL_INTERVAL_MS` | `5000` | SQS polling interval in milliseconds |

## Event Schemas

### PlaceOrderEvent

```json
{
  "eventType": "place-order-event",
  "timestamp": "2025-12-24T10:00:00Z",
  "orderId": "ORD-001",
  "customerId": "CUST-123",
  "items": [
    {
      "productId": "PROD-1",
      "quantity": 2,
      "price": 29.99
    }
  ],
  "totalAmount": 109.97
}
```

### CancelOrderEvent

```json
{
  "eventType": "cancel-order-event",
  "timestamp": "2025-12-24T10:05:00Z",
  "orderId": "ORD-001",
  "customerId": "CUST-123",
  "reason": "Customer requested cancellation"
}
```

## Project Structure

```
src/
├── main/
│   ├── kotlin/
│   │   ├── Main.kt                           # Application entry point
│   │   └── io/specmatic/async/
│   │       ├── config/
│   │       │   └── AppConfig.kt              # Configuration management
│   │       ├── model/
│   │       │   └── Events.kt                 # Event data classes
│   │       ├── processor/
│   │       │   └── EventProcessor.kt         # Event processing logic
│   │       └── service/
│   │           ├── EventBridgeListenerService.kt  # SQS polling
│   │           └── KafkaProducerService.kt        # Kafka publishing
│   └── resources/
│       └── logback.xml                       # Logging configuration
```

## Cleanup

Stop and remove all containers:

```bash
docker compose down -v
```

## Troubleshooting

### Problem: "LocalStack failed to start" or "Docker daemon is not running"

**Solution:**
1. Make sure Docker Desktop is running (on macOS/Windows) or Docker daemon is started (on Linux)
2. Check Docker status:
   ```bash
   docker info
   ```
3. If Docker is not running:
   - macOS/Windows: Open Docker Desktop application
   - Linux: `sudo systemctl start docker`

### Problem: LocalStack "Device or resource busy" error

**Symptoms:**
```
ERROR: 'rm -rf "/tmp/localstack"': exit code 1
OSError: [Errno 16] Device or resource busy: '/tmp/localstack'
```

**Solution:**
This is caused by volume mount conflicts. The fix has been applied to docker-compose.yml.
```bash
make clean      # Clean up old containers
make setup      # Start with fixed configuration
```

See `LOCALSTACK_FIX.md` for technical details.

### Problem: Services not becoming healthy

**Solution:**
1. Run the troubleshooting script:
   ```bash
   ./troubleshoot.sh
   ```
2. Check service logs:
   ```bash
   docker compose logs localstack
   docker compose logs kafka
   docker compose logs zookeeper
   ```
3. Try restarting services:
   ```bash
   docker compose down -v
   docker compose up -d
   ```

### Problem: Port already in use

**Solution:**
1. Check what's using the port (example for 4566):
   ```bash
   lsof -i :4566
   ```
2. Either stop the conflicting service or change the port in docker-compose.yml

### Check LocalStack health
```bash
curl http://localhost:4566/_localstack/health
```

### Check LocalStack logs
```bash
docker logs localstack
```

### Check Kafka logs
```bash
docker logs kafka
```

### Verify SQS queue
```bash
awslocal sqs receive-message \
  --queue-url http://localhost:4566/000000000000/order-events-queue \
  --endpoint-url http://localhost:4566
```

### List Kafka topics
```bash
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

