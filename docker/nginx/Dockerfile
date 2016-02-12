FROM nginx:1.9.11
MAINTAINER USGS LCMAP http://eros.usgs.gov

ENV DEBIAN_FRONTEND noninteractive
RUN apt-get update && apt-get upgrade -y
RUN apt-get install -y --no-install-recommends \
    apt-utils
RUN apt-get install -y --no-install-recommends \
    iptables

RUN apt-get update
RUN apt-get install --fix-missing

COPY lcmap-nginx.conf /etc/nginx/conf.d
COPY run /usr/sbin/lcmap-nginx
RUN chmod 770 /usr/sbin/lcmap-nginx

CMD ["/usr/sbin/lcmap-nginx"]
