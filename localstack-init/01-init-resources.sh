#!/bin/bash

echo "Initializing LocalStack resources..."

# Wait for LocalStack to be ready
sleep 5

# Set LocalStack endpoint and region
ENDPOINT_URL="http://localhost:4566"
REGION="us-east-1"

# Set AWS credentials (required even though LocalStack doesn't validate them)
export AWS_ACCESS_KEY_ID="test"
export AWS_SECRET_ACCESS_KEY="test"
export AWS_DEFAULT_REGION="us-east-1"

# Create EventBridge event bus
aws events create-event-bus --name order-events-bus \
  --endpoint-url=$ENDPOINT_URL \
  --region=$REGION
echo "Created EventBridge event bus: order-events-bus"

# Create SQS queue
aws sqs create-queue --queue-name order-events-queue \
  --endpoint-url=$ENDPOINT_URL \
  --region=$REGION
echo "Created SQS queue: order-events-queue"

# Get queue ARN
QUEUE_ARN=$(aws sqs get-queue-attributes \
  --queue-url http://localhost:4566/000000000000/order-events-queue \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' \
  --output text \
  --endpoint-url=$ENDPOINT_URL \
  --region=$REGION)

echo "Queue ARN: $QUEUE_ARN"

# Create EventBridge rule for place-order-event
aws events put-rule \
  --name place-order-rule \
  --event-bus-name order-events-bus \
  --event-pattern '{"detail-type":["place-order-event"]}' \
  --state ENABLED \
  --endpoint-url=$ENDPOINT_URL \
  --region=$REGION

echo "Created EventBridge rule: place-order-rule"

# Create EventBridge rule for cancel-order-event
aws events put-rule \
  --name cancel-order-rule \
  --event-bus-name order-events-bus \
  --event-pattern '{"detail-type":["cancel-order-event"]}' \
  --state ENABLED \
  --endpoint-url=$ENDPOINT_URL \
  --region=$REGION

echo "Created EventBridge rule: cancel-order-rule"

# Add SQS queue as target for place-order-rule
aws events put-targets \
  --rule place-order-rule \
  --event-bus-name order-events-bus \
  --targets "Id"="1","Arn"="$QUEUE_ARN" \
  --endpoint-url=$ENDPOINT_URL \
  --region=$REGION

echo "Added SQS target to place-order-rule"

# Add SQS queue as target for cancel-order-rule
aws events put-targets \
  --rule cancel-order-rule \
  --event-bus-name order-events-bus \
  --targets "Id"="1","Arn"="$QUEUE_ARN" \
  --endpoint-url=$ENDPOINT_URL \
  --region=$REGION

echo "Added SQS target to cancel-order-rule"

echo "LocalStack initialization complete!"

