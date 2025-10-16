args=$(filter-out $@,$(MAKECMDGOALS))

.EXPORT_ALL_VARIABLES:

ENV_FILE ?= .env
PROJECT=isu-project

ifneq (,$(wildcard .env))
  include .env
  export $(shell sed -n 's/^\([^#][^=]*\)=.*$$/\1/p' .env)
endif

all: help ## show all targets
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

help: ## show this help message
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

dev: ## start server using docker
	docker compose -f ./docker/docker-compose.yaml -p $(PROJECT) up $(args) -d ${SERVICE}

build: ## build docker image and start server
	docker compose -f ./docker/docker-compose.yaml -p $(PROJECT) up $(args) --build -d ${SERVICE}

down: ## stop docker containers
	docker compose -f ./docker/docker-compose.yaml -p $(PROJECT) down

clean: ## clean Maven and target
	mvn clean

compile: ## compile the application
	mvn compile

package: ## package the application into a JAR
	mvn package -DskipTests

run: ## run Spring Boot app locally
	mvn spring-boot:run

test: ## run tests
	mvn test

migration-up: ## apply migrations (Flyway)
	mvn flyway:migrate

migration-info: ## show Flyway migration info
	mvn flyway:info

deploy-develop: ## deploy to dev environment
	bash ./scripts/deploy.sh dev $(PROJECT)
