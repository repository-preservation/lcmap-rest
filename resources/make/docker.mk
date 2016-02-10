DOCKER_ORG=usgseros
LCMAP_REST_REPO=ubuntu-lcmap-rest
LCMAP_AUTH_REPO=ubuntu-lcmap-test-auth-server
DOCKERHUB_LCMAP_REST = $(DOCKER_ORG)/$(LCMAP_REST_REPO):$(VERSION)
DOCKERHUB_LCMAP_TEST_AUTH = $(DOCKER_ORG)/$(LCMAP_AUTH_REPO):$(VERSION)

.PHONY: docker

docker: docker-auth-build docker-server-build

docker-server-build: CONTEXT=./docker/lcmap-rest-server
docker-server-build: BUILD_DIR=$(CONTEXT)/build
docker-server-build:
	@mkdir -p $(BUILD_DIR)
	@rm -rf $(BUILD_DIR)
	-@cp -r . $(BUILD_DIR)
	-@rm -rf \
	$(BUILD_DIR)/.* \
	$(BUILD_DIR)/downloads \
	$(BUILD_DIR)/target \
	$(BUILD_DIR)/docker \
	$(BUILD_DIR)/checkouts/*
	@cp -r ../lcmap-client-clj $(BUILD_DIR)/checkouts/
	@docker build -t $(DOCKERHUB_LCMAP_REST) $(CONTEXT)
	@rm -rf $(BUILD_DIR)

docker-auth-build: CONTEXT=./docker/test-auth-server
docker-auth-build: BUILD_DIR=$(CONTEXT)/build
docker-auth-build:
	@mkdir -p $(BUILD_DIR)
	@rm -rf $(BUILD_DIR)
	-@cp -r test/support/auth-server $(BUILD_DIR)
	-@rm -rf $(BUILD_DIR)/target
	@docker build -t $(DOCKERHUB_LCMAP_TEST_AUTH) $(CONTEXT)
	@rm -rf $(BUILD_DIR)

docker-server:
	@docker run \
	-e "LCMAP_SERVER_ENV_DB_HOSTS=$(CASSANDRA_HOST):" \
	-e "LCMAP_SERVER_ENV_AUTH_USGS_ENDPOINT=$(AUTH_ENDPOINT)" \
	-e "LCMAP_USERNAME=alice" \
	-e "LCMAP_PASSWORD=secret" \
	-e "LCMAP_ENDPOINT=http://localhost:1077" \
	-e "LCMAP_VERSION=0.0" \
	-e "LCMAP_LOG_LEVEL=debug" \
	-e "LCMAP_CONTENT_TYPE=json" \
	-t $(DOCKERHUB_LCMAP_REST)

docker-server-publish:
	@docker push $(DOCKERHUB_LCMAP_REST)

docker-server-bash:
	@docker run \
	-e "LCMAP_SERVER_ENV_DB_HOSTS=$(CASSANDRA_HOST)" \
	-e "LCMAP_USERNAME=alice" \
	-e "LCMAP_PASSWORD=secret" \
	-e "LCMAP_ENDPOINT=http://localhost:1077" \
	-e "LCMAP_VERSION=0.0" \
	-e "LCMAP_LOG_LEVEL=debug" \
	-e "LCMAP_CONTENT_TYPE=json" \
	-it --entrypoint=/bin/bash $(DOCKERHUB_LCMAP_REST) -s

docker-server-repl:
	@docker run \
	-e "LCMAP_SERVER_ENV_DB_HOSTS=$(CASSANDRA_HOST)" \
	-e "LCMAP_USERNAME=alice" \
	-e "LCMAP_PASSWORD=secret" \
	-e "LCMAP_ENDPOINT=http://localhost:1077" \
	-e "LCMAP_VERSION=0.0" \
	-e "LCMAP_LOG_LEVEL=debug" \
	-e "LCMAP_CONTENT_TYPE=json" \
	-it --entrypoint=/lcmap-rest/bin/repl \
	$(DOCKERHUB_LCMAP_REST)

docker-auth:
	@docker run -t $(DOCKERHUB_LCMAP_TEST_AUTH)

docker-auth-bash:
	@docker run -it --entrypoint=/bin/bash $(DOCKERHUB_LCMAP_TEST_AUTH) -s

docker-auth-publish:
	@docker push $(DOCKERHUB_LCMAP_TEST_AUTH)

docker-publish: docker-server-publish docker-auth-publish

dockerhub: docker docker-publish

docker-clean:
	-@docker rm $(shell docker ps -a -q)
	-@docker rmi $(shell docker images -q --filter 'dangling=true')
