FROM usgseros/ubuntu-lcmap-rest:0.0.1
MAINTAINER USGS LCMAP http://eros.usgs.gov

RUN mkdir /root/.usgs
COPY build/lcmap.ini /root/.usgs/

RUN apt-get update
RUN apt-get install -y --fix-missing
RUN apt-get install -y libgsl0-dev libgsl0ldbl gsl-bin \
    libmatio-dev libmatio2 gfortran
RUN ln -s `which gfortran-4.8`
RUN git clone https://github.com/USGS-EROS/lcmap-change-detection-c.git
RUN cd lcmap-change-detection-c && \
    BIN=/usr/bin make
