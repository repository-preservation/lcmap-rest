FROM usgseros/ubuntu-gis-clj:0.5.0
MAINTAINER USGS LCMAP http://eros.usgs.gov

RUN mkdir /lcmap-rest
COPY build /lcmap-rest
WORKDIR /lcmap-rest

VOLUME ["/root/.usgs"]

ENTRYPOINT ["java", "-javaagent:/lcmap-rest/quasar-core-0.7.5.jar", "-jar", "/lcmap-rest/lcmap-rest-0.5.1-SNAPSHOT-standalone.jar"]
