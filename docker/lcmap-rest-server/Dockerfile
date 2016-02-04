FROM usgseros/ubuntu-gis-clj
MAINTAINER USGS LCMAP http://eros.usgs.gov

RUN echo cahe bust

COPY build /lcmap-rest
RUN cd /lcmap-rest && lein deps && lein compile

ENTRYPOINT ["/lcmap-rest/bin/run"]
