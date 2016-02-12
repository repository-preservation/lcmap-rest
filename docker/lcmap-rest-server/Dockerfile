FROM usgseros/ubuntu-gis-clj
MAINTAINER USGS LCMAP http://eros.usgs.gov

COPY build /lcmap-rest
RUN cd /lcmap-rest && lein deps && lein compile

ENTRYPOINT ["/lcmap-rest/bin/run"]
