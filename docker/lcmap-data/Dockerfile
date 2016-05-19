FROM usgseros/ubuntu-gis-clj
MAINTAINER USGS LCMAP http://eros.usgs.gov
LABEL Description="This image is used to initialize the LCMAP DB schema and ingest ARD"

RUN mkdir /root/.usgs
COPY build/lcmap.ini /root/.usgs/

RUN mkdir -p /app/data

WORKDIR /app
RUN git clone https://github.com/USGS-EROS/lcmap-data-clj

WORKDIR /app/lcmap-data-clj
RUN lein deps
