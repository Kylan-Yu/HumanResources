.PHONY: help build up down logs clean test health

# Default target
help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Targets:'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-15s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

# Build all services
build: ## Build all Docker images
	@echo "Building all services..."
	docker-compose build --no-cache

# Start all services
up: ## Start all services
	@echo "Starting all services..."
	docker-compose up -d

# Stop all services
down: ## Stop all services
	@echo "Stopping all services..."
	docker-compose down

# Show logs
logs: ## Show logs for all services
	docker-compose logs -f

# Show logs for specific service
logs-service: ## Show logs for specific service (usage: make logs-service SERVICE=hrms-auth)
	@if [ -z "$(SERVICE)" ]; then \
		echo "Usage: make logs-service SERVICE=<service-name>"; \
		exit 1; \
	fi
	docker-compose logs -f $(SERVICE)

# Clean up
clean: ## Remove all containers, networks, and volumes
	@echo "Cleaning up..."
	docker-compose down -v --remove-orphans
	docker system prune -f

# Test all services
test: ## Run tests for all services
	@echo "Running tests..."
	docker-compose exec hrms-auth mvn test
	docker-compose exec hrms-org mvn test
	docker-compose exec hrms-employee mvn test
	docker-compose exec hrms-payroll mvn test
	docker-compose exec hrms-recruit mvn test

# Health check
health: ## Check health of all services
	@echo "Checking service health..."
	@docker-compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"

# Restart specific service
restart-service: ## Restart specific service (usage: make restart-service SERVICE=hrms-auth)
	@if [ -z "$(SERVICE)" ]; then \
		echo "Usage: make restart-service SERVICE=<service-name>"; \
		exit 1; \
	fi
	docker-compose restart $(SERVICE)

# Scale service
scale: ## Scale service (usage: make scale SERVICE=hrms-auth REPLICAS=3)
	@if [ -z "$(SERVICE)" ] || [ -z "$(REPLICAS)" ]; then \
		echo "Usage: make scale SERVICE=<service-name> REPLICAS=<number>"; \
		exit 1; \
	fi
	docker-compose up -d --scale $(SERVICE)=$(REPLICAS)

# Backup database
backup: ## Backup MySQL database
	@echo "Creating database backup..."
	mkdir -p backups
	docker-compose exec mysql mysqldump -u root -proot hrms > backups/hrms-backup-$(shell date +%Y%m%d-%H%M%S).sql

# Restore database
restore: ## Restore database from backup (usage: make restore BACKUP_FILE=backup.sql)
	@if [ -z "$(BACKUP_FILE)" ]; then \
		echo "Usage: make restore BACKUP_FILE=<backup-file>"; \
		exit 1; \
	fi
	@echo "Restoring database from $(BACKUP_FILE)..."
	docker-compose exec -T mysql mysql -u root -proot hrms < $(BACKUP_FILE)

# Access service shell
shell: ## Access service shell (usage: make shell SERVICE=hrms-auth)
	@if [ -z "$(SERVICE)" ]; then \
		echo "Usage: make shell SERVICE=<service-name>"; \
		exit 1; \
	fi
	docker-compose exec $(SERVICE) /bin/bash

# View service logs
view-logs: ## View logs for specific service (usage: make view-logs SERVICE=hrms-auth)
	@if [ -z "$(SERVICE)" ]; then \
		echo "Usage: make view-logs SERVICE=<service-name>"; \
		exit 1; \
	fi
	docker-compose logs -f --tail=100 $(SERVICE)

# Monitor resources
monitor: ## Monitor resource usage
	@echo "Resource usage:"
	@docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}"

# Quick start (build + up)
quick-start: build up ## Quick start the entire stack

# Full restart (down + build + up)
full-restart: down build up ## Full restart of the entire stack

# Development mode (with hot reload)
dev: ## Start in development mode
	@echo "Starting in development mode..."
	docker-compose -f docker-compose.yml -f docker-compose.dev.yml up

# Production mode
prod: ## Start in production mode
	@echo "Starting in production mode..."
	docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
