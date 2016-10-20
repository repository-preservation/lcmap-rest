#!/usr/bin/env bash

mkdir checkouts
cd checkouts && \
    git clone https://github.com/USGS-EROS/lcmap-config.git && \
    git clone https://github.com/USGS-EROS/lcmap-logger.git && \
    git clone https://github.com/USGS-EROS/lcmap-event.git && \
    git clone https://github.com/USGS-EROS/lcmap-data.git && \
    git clone https://github.com/USGS-EROS/lcmap-see.git && \
    # delete the following line once the topic/rename-eventd branch is merged
    # to lcmap-see/master
    cd lcmap-see && git checkout topic/mesos-docker-model && cd - && \
    git clone https://github.com/USGS-EROS/lcmap-client-clj.git && \
    # delete the following line once the topic/rename-eventd branch is merged
    # to lcmap-see/master
    cd lcmap-client-clj && git checkout topic/mesos-docker-model && cd - && \
    cd ../

sudo apt-add-repository ppa:ubuntugis/ppa -y
sudo apt-get update -qq
sudo apt-get install libgdal-dev libgdal-java -y

mkdir ~/.usgs/
cp test/support/lcmap.test.ini.example ~/.usgs/lcmap.test.ini
cp test/support/lcmap.test.ini.example ~/.usgs/lcmap.ini

