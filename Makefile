VERSION=0.1.0
PROJECT=lcmap-rest
STANDALONE=target/$(PROJECT)-$(VERSION)-SNAPSHOT-standalone.jar
ROOT_DIR = $(shell pwd)
DOCS_DIR = $(ROOT_DIR)/docs
REPO = $(shell git config --get remote.origin.url)
DOCS_BUILD_DIR = $(DOCS_DIR)/build
DOCS_PROD_DIR = $(DOCS_DIR)/master
CURRENT = $(DOCS_PROD_DIR)/current
ERL_DOCS_SRC = ~/Dropbox/Docs/Erlang
ERL_DOCS_DIR = $(CURRENT)/erlang
JAVA_DOCS_DIR = $(ERL_DOCS_DIR)/java
DOCS_GIT_HACK = $(DOCS_DIR)/.git
LOCAL_DOCS_HOST = localhost
LOCAL_DOCS_PORT = 5099

DOCKERHUB_LCMAP_REST = usgseros/ubuntu-lcmap-rest
DOCKERHUB_LCMAP_TEST_AUTH = usgseros/ubuntu-lcmap-test-auth-server

.PHONY: docs docker

$(DOCS_GIT_HACK):
	-@ln -s $(ROOT_DIR)/.git $(DOCS_DIR)

devdocs: docs
	@echo "\nRunning docs server on http://$(LOCAL_DOCS_HOST):$(LOCAL_DOCS_PORT)..."
	@lein simpleton $(LOCAL_DOCS_PORT) file :from $(CURRENT)

docs: clean-docs local-docs

local-docs: pre-docs clojure-docs

prod-docs: clean-docs $(DOCS_GIT_HACK) local-docs

pre-docs:
	@echo "\nBuilding docs ...\n"

clojure-docs:
	@lein codox

clean-docs: clean
	@rm -rf $(CURRENT)

setup-temp-repo: $(DOCS_GIT_HACK)
	@echo "\nSetting up temporary git repos for gh-pages ...\n"
	@rm -rf $(DOCS_PROD_DIR)/.git $(DOCS_PROD_DIR)/*/.git
	@cd $(DOCS_PROD_DIR) && git init
	@cd $(DOCS_PROD_DIR) && git add * > /dev/null
	@cd $(DOCS_PROD_DIR) && git commit -a -m "Generated content." > /dev/null

teardown-temp-repo:
	@echo "\nTearing down temporary gh-pages repos ..."
	@rm $(DOCS_DIR)/.git
	@rm -rf $(DOCS_PROD_DIR)/.git $(DOCS_PROD_DIR)/*/.git

publish-docs: prod-docs setup-temp-repo
	@echo "\nPublishing docs ...\n"
	@cd $(DOCS_PROD_DIR) && git push -f $(REPO) master:gh-pages
	@make teardown-temp-repo

clean-all: clean docker-clean

clean:
	@rm -rf target

build: clean
	@lein compile
	@lein uberjar

shell:
	@lein repl

run:
	@lein run

standalone: build
	java -jar $(STANDALONE)

standalone-heavy: build
	java -Xms3072m -Xmx3072m -jar $(STANDALONE)

test-auth-server:
	@cd test/support/auth-server && lein with-profile +dev run

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

dockerhub: docker docker-server-publish docker-auth-publish

docker-clean:
	-@docker rm $(shell docker ps -a -q)
	-@docker rmi $(shell docker images -q --filter 'dangling=true')
