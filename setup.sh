#!/bin/bash

set -e

echo "=========================================="
echo "EventBridge to Kafka Bridge - Setup Script"
echo "=========================================="

# Check prerequisites
echo "Checking prerequisites..."

if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi
echo "✅ Docker found"

# Check if Docker daemon is running
if ! docker info &> /dev/null; then
    echo "❌ Docker daemon is not running. Please start Docker Desktop or Docker daemon."
    exit 1
fi
echo "✅ Docker daemon is running"

if ! command -v docker compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi
echo "✅ Docker Compose found"

if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi
echo "✅ Java found: $(java -version 2>&1 | head -n 1)"

# Clean up any previous containers
echo ""
echo "Cleaning up previous containers..."
docker compose down -v 2>/dev/null || true

# Clean up tmp/localstack directory if it exists (prevents "device busy" issues)
if [ -d "./tmp/localstack" ]; then
    echo "Removing old tmp/localstack directory..."
    rm -rf ./tmp/localstack 2>/dev/null || true
fi

# Start services
echo ""
echo "Starting LocalStack and Kafka services..."
docker compose up -d

# Wait for services to be ready
echo ""
echo "Waiting for services to be ready..."

# Function to check service health
check_service_health() {
    service_name=$1
    docker compose ps --format json | grep -q "\"Service\":\"$service_name\".*\"Health\":\"healthy\""
}

# Wait for LocalStack
echo "Waiting for LocalStack to be healthy..."
max_wait=60
elapsed=0
while [ $elapsed -lt $max_wait ]; do
    if docker compose ps | grep localstack | grep -q "healthy"; then
        echo "✅ LocalStack is healthy"
        break
    fi

    if [ $elapsed -eq 0 ]; then
        echo -n "  Waiting"
    fi
    echo -n "."
    sleep 2
    elapsed=$((elapsed + 2))
done
echo ""

if [ $elapsed -ge $max_wait ]; then
    echo "⚠️  LocalStack health check timed out, but continuing..."
fi

# Wait for Kafka
echo "Waiting for Kafka to be healthy..."
elapsed=0
while [ $elapsed -lt $max_wait ]; do
    if docker compose ps | grep kafka | grep -q "healthy"; then
        echo "✅ Kafka is healthy"
        break
    fi

    if [ $elapsed -eq 0 ]; then
        echo -n "  Waiting"
    fi
    echo -n "."
    sleep 2
    elapsed=$((elapsed + 2))
done
echo ""

if [ $elapsed -ge $max_wait ]; then
    echo "⚠️  Kafka health check timed out, but continuing..."
fi

# Verify LocalStack is ready
echo ""
echo "Verifying LocalStack..."
max_attempts=10
attempt=0
until [ $attempt -eq $max_attempts ]; do
    # Check if container is running
    if ! docker ps | grep -q localstack; then
        echo "  ❌ LocalStack container is not running!"
        echo ""
        echo "Checking container logs:"
        docker logs localstack 2>&1 | tail -20
        exit 1
    fi

    # Try to access LocalStack health endpoint
    if curl -s http://localhost:4566/_localstack/health > /dev/null 2>&1; then
        echo "✅ LocalStack is ready"
        break
    fi

    # Alternative check using aws CLI
    if docker exec localstack aws sqs list-queues --endpoint-url=http://localhost:4566 --region=us-east-1 &>/dev/null; then
        echo "✅ LocalStack is ready"
        break
    fi

    attempt=$((attempt+1))
    if [ $attempt -lt $max_attempts ]; then
        echo "  Attempt $attempt/$max_attempts - LocalStack not ready yet..."
        sleep 5
    fi
done

if [ $attempt -eq $max_attempts ]; then
    echo "❌ LocalStack failed to start"
    echo ""
    echo "LocalStack container logs:"
    docker logs localstack 2>&1 | tail -30
    exit 1
fi

# Verify Kafka is ready
echo ""
echo "Verifying Kafka..."
max_attempts=10
attempt=0
until docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list &>/dev/null || [ $attempt -eq $max_attempts ]; do
    echo "  Attempt $((attempt+1))/$max_attempts - Kafka not ready yet..."
    sleep 5
    attempt=$((attempt+1))
done

if [ $attempt -eq $max_attempts ]; then
    echo "❌ Kafka failed to start"
    exit 1
fi
echo "✅ Kafka is ready"

# Create Kafka topics
echo ""
echo "Creating Kafka topics..."

echo "Creating 'place-order' topic..."
docker exec kafka kafka-topics --create \
    --bootstrap-server localhost:9092 \
    --replication-factor 1 \
    --partitions 3 \
    --topic place-order \
    --if-not-exists

echo "Creating 'cancel-order' topic..."
docker exec kafka kafka-topics --create \
    --bootstrap-server localhost:9092 \
    --replication-factor 1 \
    --partitions 3 \
    --topic cancel-order \
    --if-not-exists

echo "✅ Kafka topics created"

# Verify AWS resources were created
echo ""
echo "Verifying AWS resources..."
if aws sqs get-queue-url --queue-name order-events-queue --endpoint-url=http://localhost:4566 --region=us-east-1 &>/dev/null; then
    echo "✅ SQS queue 'order-events-queue' exists"
else
    echo "⚠️  SQS queue not found. Running initialization script manually..."
    docker exec -e AWS_ACCESS_KEY_ID=test -e AWS_SECRET_ACCESS_KEY=test -e AWS_DEFAULT_REGION=us-east-1 \
        localstack bash /etc/localstack/init/ready.d/01-init-resources.sh
fi

if aws events describe-event-bus --name order-events-bus --endpoint-url=http://localhost:4566 --region=us-east-1 &>/dev/null; then
    echo "✅ EventBridge bus 'order-events-bus' exists"
else
    echo "⚠️  EventBridge bus not found. Please check the logs above."
fi

echo ""
echo "=========================================="
echo "✅ Setup complete!"
echo "=========================================="
echo ""
echo "Services running:"
echo "- LocalStack (EventBridge & SQS): http://localhost:4566"
echo "- Kafka: localhost:9092"
echo "- Kafka UI: http://localhost:8080"
echo ""
echo "Next steps:"
echo "1. Build the application: ./gradlew build"
echo "2. Run the application: ./gradlew run"
echo "3. In another terminal, send test events: ./scripts/send-test-events.sh"
echo "4. Monitor Kafka messages: ./scripts/consume-kafka-messages.sh"
echo ""

