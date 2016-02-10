VERSION=0.0.1
PROJECT=lcmap-rest
STANDALONE=target/$(PROJECT)-$(VERSION)-SNAPSHOT-standalone.jar
ROOT_DIR = $(shell pwd)

build: clean
	@lein compile
	@lein uberjar

standalone: build
	java -jar $(STANDALONE)

standalone-heavy: build
	java -Xms3072m -Xmx3072m -jar $(STANDALONE)

run:
	@lein run

test-auth-server:
	@cd test/support/auth-server && lein with-profile +dev run

shell:
	@lein repl

repl:
	@lein repl

clean-all: clean docker-clean

clean:
	@rm -rf target

include resources/make/docs.mk
include resources/make/docker.mk
