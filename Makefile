.PHONY: help check setup build run test clean start-infra stop-infra send-events consume-events docker-logs troubleshoot

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

check: ## Check prerequisites (Docker, Java, ports)
	@./check-prerequisites.sh

setup: ## Setup infrastructure (LocalStack + Kafka) - Full automated setup
	@echo "Setting up infrastructure..."
	@./setup.sh

build: ## Build the application
	@echo "Building application..."
	@./gradlew build

run: ## Run the application
	@echo "Running application..."
	@./gradlew run

test: ## Run tests
	@echo "Running tests..."
	@./gradlew test

clean: ## Clean build artifacts
	@echo "Cleaning..."
	@./gradlew clean
	@docker compose down -v

start-infra: ## Start LocalStack and Kafka (without full setup)
	@echo "Starting infrastructure..."
	@./start.sh

stop-infra: ## Stop LocalStack and Kafka
	@echo "Stopping infrastructure..."
	@docker compose down

send-events: ## Send test events to EventBridge
	@echo "Sending test events..."
	@./scripts/send-test-events.sh

consume-events: ## Consume events from Kafka
	@echo "Consuming events from Kafka..."
	@./scripts/consume-kafka-messages.sh

docker-logs: ## Show logs from all containers
	@docker compose logs -f

troubleshoot: ## Run troubleshooting diagnostics
	@./troubleshoot.sh

all: clean setup build run ## Clean, setup, build and run everything

