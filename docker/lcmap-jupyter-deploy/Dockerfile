FROM usgseros/ubuntu-gis-notebooks
MAINTAINER USGS LCMAP http://eros.usgs.gov

RUN apt-get install -y python3-dev python-gdal \
    python-pandas python-pandas-lib python3-pandas python3-pandas-lib
RUN wget https://bootstrap.pypa.io/get-pip.py && \
    python3 get-pip.py && \
    python2 get-pip.py && \
    rm get-pip.py
RUN echo cache bust
RUN git clone https://github.com/USGS-EROS/lcmap-client-py.git
RUN cd lcmap-client-py && \
    pip3 install -U -rrequirements/base.txt . && \
    pip2 install -U -rrequirements/base.txt .
