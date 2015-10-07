VERSION=0.1.0
PROJECT=lcmap-rest
STANDALONE=target/$(PROJECT)-$(VERSION)-SNAPSHOT-standalone.jar

clean:
	rm -rf target

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
