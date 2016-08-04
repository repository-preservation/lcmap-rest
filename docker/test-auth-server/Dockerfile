FROM usgseros/ubuntu-gis-clj:0.5.0
MAINTAINER USGS LCMAP http://eros.usgs.gov

COPY build /lcmap-auth
RUN cd /lcmap-auth && lein deps && lein compile

ENTRYPOINT ["/lcmap-auth/bin/run"]
