#!/bin/bash

# This script sends test events to EventBridge

echo "Sending PlaceOrderEvent to EventBridge..."

aws events put-events \
  --entries '[
    {
      "Source": "order-service",
      "DetailType": "place-order-event",
      "Detail": "{\"eventType\":\"place-order-event\",\"timestamp\":\"2025-12-24T10:00:00Z\",\"orderId\":\"ORD-001\",\"customerId\":\"CUST-123\",\"items\":[{\"productId\":\"PROD-1\",\"quantity\":2,\"price\":29.99},{\"productId\":\"PROD-2\",\"quantity\":1,\"price\":49.99}],\"totalAmount\":109.97}",
      "EventBusName": "order-events-bus"
    }
  ]' \
  --endpoint-url=http://localhost:4566 \
  --region=us-east-1

echo "PlaceOrderEvent sent!"

sleep 2

echo "Sending CancelOrderEvent to EventBridge..."

aws events put-events \
  --entries '[
    {
      "Source": "order-service",
      "DetailType": "cancel-order-event",
      "Detail": "{\"eventType\":\"cancel-order-event\",\"timestamp\":\"2025-12-24T10:05:00Z\",\"orderId\":\"ORD-001\",\"customerId\":\"CUST-123\",\"reason\":\"Customer requested cancellation\"}",
      "EventBusName": "order-events-bus"
    }
  ]' \
  --endpoint-url=http://localhost:4566 \
  --region=us-east-1

echo "CancelOrderEvent sent!"

echo "Check the application logs to see the events being processed"

