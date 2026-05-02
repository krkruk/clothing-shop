.PHONY: infra-local build dev run run-fresh clean clean-all seed seed-api test test-component test-acceptance

COMPOSE = podman compose
INFRA_DIR = infra

infra-local:
	$(COMPOSE) -f $(INFRA_DIR)/compose.local.yml up -d

build:
	$(COMPOSE) -f $(INFRA_DIR)/compose.yml build

dev:
	$(COMPOSE) -f $(INFRA_DIR)/compose.yml up --build

run:
	$(COMPOSE) -f $(INFRA_DIR)/compose.yml up -d

run-fresh: clean-all generate build run

clean:
	$(COMPOSE) -f $(INFRA_DIR)/compose.yml down -v
	$(COMPOSE) -f $(INFRA_DIR)/compose.local.yml down -v

clean-all: clean
	cd backend && ./gradlew clean
	rm -rf frontend/.svelte-kit frontend/build frontend/node_modules
	rm -rf frontend/src/api/generated/!(.gitkeep)

seed:
	bash $(INFRA_DIR)/seed.sh

seed-api:
	bash $(INFRA_DIR)/seed-api.sh

generate:
	cd backend && ./gradlew openApiGenerate
	cd frontend && npm run generate:api

test:
	cd backend && ./gradlew test

test-component:
	cd backend && ./gradlew testComponent

test-acceptance:
	cd acceptance_test && uv run pytest -v
