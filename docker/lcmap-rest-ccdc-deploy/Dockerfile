FROM usgseros/ubuntu-lcmap-rest:0.0.2
MAINTAINER USGS LCMAP http://eros.usgs.gov

RUN mkdir /root/.usgs
COPY build/lcmap.ini /root/.usgs/

RUN apt-get update
RUN apt-get install -y --fix-missing
RUN apt-get install -y libgsl0-dev libgsl0ldbl gsl-bin \
    libmatio-dev libmatio2 gfortran
RUN apt-get install -y ncurses-base ncurses-term \
    libncurses5-dev libncurses5 libreadline-dev readline-common
RUN apt-get install -y python3-dev python-gdal \
    python-pandas python-pandas-lib python3-pandas python3-pandas-lib
RUN wget https://bootstrap.pypa.io/get-pip.py && \
    python3 get-pip.py && \
    python get-pip.py && \
    rm get-pip.py
RUN git clone https://github.com/USGS-EROS/lcmap-change-detection-c.git
RUN cd lcmap-change-detection-c && \
    BIN=/usr/bin make
RUN echo cache bust
RUN git clone https://github.com/USGS-EROS/lcmap-client-py.git
RUN cd lcmap-client-py && \
    pip3 install -U -rrequirements/base.txt . && \
    pip install -U -rrequirements/base.txt .
