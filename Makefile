VERSION=0.0.2
PROJECT=lcmap-rest
STANDALONE=target/$(PROJECT)-$(VERSION)-SNAPSHOT-standalone.jar
ROOT_DIR = $(shell pwd)

include resources/make/code.mk
include resources/make/docs.mk
include resources/make/docker.mk

