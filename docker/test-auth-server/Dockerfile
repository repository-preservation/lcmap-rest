FROM usgseros/ubuntu-gis-clj
MAINTAINER USGS LCMAP http://eros.usgs.gov

COPY build /lcmap-auth
RUN cd /lcmap-auth && lein deps && lein compile

ENTRYPOINT ["/lcmap-auth/bin/run"]
