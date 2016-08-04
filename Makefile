VERSION=0.5.1-SNAPSHOT
PROJECT=lcmap-rest
STANDALONE=target/uberjar/$(PROJECT)-$(VERSION)-standalone.jar
ROOT_DIR = $(shell pwd)

include resources/make/code.mk
include resources/make/docs.mk
include resources/make/docker.mk
