#!/usr/bin/env bash

mkdir checkouts
cd checkouts && \
    git clone https://github.com/USGS-EROS/lcmap-config.git && \
    git clone https://github.com/USGS-EROS/lcmap-logger.git && \
    git clone https://github.com/USGS-EROS/lcmap-event.git && \
    git clone https://github.com/USGS-EROS/lcmap-data.git && \
    git clone https://github.com/USGS-EROS/lcmap-see.git && \
    git clone https://github.com/USGS-EROS/lcmap-client-clj.git && \
    cd ../

mkdir ~/.usgs/
cp test/support/lcmap.test.ini.example ~/.usgs/lcmap.test.ini
cp test/support/lcmap.test.ini.example ~/.usgs/lcmap.ini

