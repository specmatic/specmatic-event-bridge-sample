# Contract Testing with Specmatic

This directory contains contract tests for the EventBridge to Kafka bridge application using Specmatic.

## Overview

The contract test validates that the application correctly:
1. Receives events from AWS EventBridge (via SQS queue)
2. Processes the events according to the AsyncAPI specification
3. Publishes transformed messages to the correct Kafka topics

## Test Structure

### ContractTest.kt

The main test class that:
- Uses Testcontainers to start infrastructure (LocalStack, Kafka, Zookeeper)
- Initializes AWS resources (EventBridge bus, SQS queue, routing rules)
- Starts the EventBridge to Kafka bridge application
- Runs Specmatic contract tests against the running application
- Verifies all contract expectations are met

### Test Flow

```
┌─────────────────────────────────────────────────────┐
│ 1. Start Infrastructure (docker-compose)           │
│    - LocalStack (EventBridge + SQS)                 │
│    - Kafka + Zookeeper                              │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│ 2. Initialize AWS Resources                         │
│    - Create EventBridge bus                         │
│    - Create SQS queue                               │
│    - Create routing rules                           │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│ 3. Start Application                                │
│    - EventBridge listener (polls SQS)               │
│    - Event processor                                │
│    - Kafka producer                                 │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│ 4. Run Specmatic Tests                              │
│    - Load AsyncAPI spec                             │
│    - Load test examples                             │
│    - Send events to EventBridge                     │
│    - Verify Kafka messages                          │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│ 5. Verify Results                                   │
│    - Check test report                              │
│    - Assert all tests passed                        │
└─────────────────────────────────────────────────────┘
```

## Running the Tests

### Prerequisites

- Docker and Docker Compose running
- Java 17 or higher
- Gradle (via wrapper)

### Run Tests

```bash
# Run all tests
./gradlew test

# Run only contract tests
./gradlew test --tests ContractTest

# Run with verbose output
./gradlew test --tests ContractTest --info
```

### CI/CD Configuration

The test includes a platform check:
```kotlin
@EnabledIf(value = "isNonCIOrLinux", disabledReason = "Run only on Linux in CI; all platforms allowed locally")
```

This ensures:
- **Local development**: Tests run on all platforms (macOS, Linux, Windows)
- **CI environment**: Tests run only on Linux (for consistency with containers)

## Configuration

### specmatic.yaml

The Specmatic configuration file defines:

```yaml
version: 2
contracts:
  - provides:
      - specs:
          - spec/order-events-async-api.yaml
        specType: asyncapi
        config:
          servers:
            - host: http://localhost:4566
              protocol: eventbridge
              adminCredentials:
                region: us-east-1
                aws.access.key.id: test
                aws.secret.access.key: test
            - host: localhost:9092
              protocol: kafka
```

**Key points:**
- Uses the AsyncAPI specification from `spec/order-events-async-api.yaml`
- Connects to LocalStack EventBridge at `http://localhost:4566`
- Connects to Kafka at `localhost:9092`
- Uses test credentials for LocalStack

## Test Scenarios

The contract tests validate scenarios defined in `spec/examples/`:

1. **place-order-flow.json**
   - Standard place order event
   - Verifies routing to `place-order` Kafka topic

2. **cancel-order-flow.json**
   - Standard cancel order event
   - Verifies routing to `cancel-order` Kafka topic

3. **large-order-flow.json**
   - Large order with multiple items
   - Tests complex payload handling

4. **urgent-cancel-flow.json**
   - Urgent cancellation scenario
   - Tests edge cases

## Test Reports

Test reports are generated in:
- **Console**: Real-time test output
- **JUnit XML**: `build/test-results/test/`
- **Specmatic Reports**: `build/reports/specmatic/`

## Debugging Failed Tests

### View Specmatic Logs

The test outputs Specmatic container logs directly to the console. Look for:
- Failed assertions
- Message format errors
- Connectivity issues

### Check Infrastructure Logs

```bash
# LocalStack logs
docker logs localstack

# Kafka logs
docker logs kafka

# Application logs
# Check test output in console
```

### Common Issues

1. **Infrastructure not ready**
   - Increase wait times in test setup
   - Check Docker resource allocation

2. **AWS resources not initialized**
   - Verify init script execution
   - Check LocalStack logs

3. **Kafka connectivity issues**
   - Ensure Kafka is healthy before tests
   - Check network mode (host) in Testcontainers

4. **Message format errors**
   - Validate AsyncAPI spec
   - Check example JSON files
   - Review event processor logic

## Dependencies

Required test dependencies (in `build.gradle.kts`):

```kotlin
testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
testImplementation("org.testcontainers:testcontainers:1.19.3")
testImplementation("org.testcontainers:junit-jupiter:1.19.3")
testImplementation("org.testcontainers:kafka:1.19.3")
testImplementation("org.assertj:assertj-core:3.24.2")
```

## Best Practices

1. **Keep examples up-to-date**: Ensure `spec/examples/` matches real scenarios
2. **Maintain AsyncAPI spec**: Keep `spec/order-events-async-api.yaml` synchronized with code
3. **Test locally first**: Run tests locally before pushing to CI
4. **Review logs**: Always check Specmatic output for contract violations
5. **Update timeouts**: Adjust wait times based on system performance

## Further Reading

- [Specmatic Documentation](https://specmatic.in/)
- [AsyncAPI Specification](https://www.asyncapi.com/)
- [Testcontainers Documentation](https://www.testcontainers.org/)

