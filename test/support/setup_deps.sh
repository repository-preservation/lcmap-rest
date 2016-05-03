#!/usr/bin/env bash

mkdir checkouts
cd checkouts && git clone https://github.com/USGS-EROS/lcmap-config.git && cd -
cd checkouts && git clone https://github.com/USGS-EROS/lcmap-client-clj.git && cd -
cd checkouts && git clone https://github.com/USGS-EROS/lcmap-data.git && cd -
cd checkouts && git clone https://github.com/USGS-EROS/lcmap-see.git && cd -
cd checkouts && git clone https://github.com/USGS-EROS/lcmap-event.git && cd -
mkdir ~/.usgs/
cp test/support/sample_config.ini ~/.usgs/lcmap.ini
