#!/bin/bash

# Script to validate the AsyncAPI specification

echo "Validating AsyncAPI specification..."

# Check if asyncapi CLI is installed
if ! command -v asyncapi &> /dev/null; then
    echo "❌ AsyncAPI CLI is not installed."
    echo ""
    echo "To install, run:"
    echo "  npm install -g @asyncapi/cli"
    echo ""
    exit 1
fi

# Validate the specification
asyncapi validate spec/order-events-async-api.yaml

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ AsyncAPI specification is valid!"
    echo ""
    echo "To generate HTML documentation, run:"
    echo "  asyncapi generate fromTemplate spec/order-events-async-api.yaml @asyncapi/html-template -o docs/asyncapi"
else
    echo ""
    echo "❌ AsyncAPI specification validation failed!"
    exit 1
fi

