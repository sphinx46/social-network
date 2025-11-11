.PHONY: help build up down logs clean restart rebuild test

GREEN=\033[0;32m
YELLOW=\033[1;33m
RED=\033[0;31m
NC=\033[0m

help:
	@echo "$(YELLOW)Social Network Monolith Docker Commands:$(NC)"
	@echo ""
	@echo "$(GREEN)build$(NC)    - Build the application and Docker images"
	@echo "$(GREEN)up$(NC)      - Start all services in detached mode"
	@echo "$(GREEN)down$(NC)    - Stop and remove all containers"
	@echo "$(GREEN)logs$(NC)    - Show logs from all services"
	@echo "$(GREEN)clean$(NC)   - Remove all containers, volumes and images"
	@echo "$(GREEN)restart$(NC) - Restart all services"
	@echo "$(GREEN)rebuild$(NC) - Rebuild and restart all services"
	@echo "$(GREEN)test$(NC)    - Run tests"
	@echo "$(GREEN)status$(NC)  - Show running containers"

build:
	@echo "$(YELLOW)Building application...$(NC)"
	mvn clean package -DskipTests
	@echo "$(YELLOW)Building Docker images...$(NC)"
	docker-compose build

up:
	@echo "$(YELLOW)Starting services...$(NC)"
	docker-compose up -d

down:
	@echo "$(YELLOW)Stopping services...$(NC)"
	docker-compose down

logs:
	docker-compose logs -f

clean:
	@echo "$(YELLOW)Cleaning up...$(NC)"
	docker-compose down -v
	docker system prune -f

restart: down up

rebuild: build restart

test:
	@echo "$(YELLOW)Running tests...$(NC)"
	mvn test

status:
	@echo "$(YELLOW)Running containers:$(NC)"
	docker-compose ps

db-shell:
	docker exec -it social-network-postgres psql -U postgres -d social-network

db-backup:
	@echo "$(YELLOW)Creating database backup...$(NC)"
	docker exec social-network-postgres pg_dump -U postgres social-network > backup_$(shell date +%Y%m%d_%H%M%S).sql

db-restore:
	@echo "$(YELLOW)Restoring database from backup...$(NC)"
	@if [ -z "$(FILE)" ]; then \
		echo "$(RED)Please specify backup file: make db-restore FILE=backup.sql$(NC)"; \
		exit 1; \
	fi
	docker exec -i social-network-postgres psql -U postgres -d social-network < $(FILE)

health:
	@echo "$(YELLOW)Checking application health...$(NC)"
	@curl -f http://localhost:8080/actuator/health || echo "$(RED)Application is not healthy$(NC)"

dev:
	@echo "$(YELLOW)Starting development mode...$(NC)"
	mvn spring-boot:run

deploy: build up health
	@echo "$(GREEN)Deployment completed successfully!$(NC)"