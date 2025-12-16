FROM ubuntu:22.04

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
            apache2 curl wget \
            ca-certificates \
            libjansson4 \
            libapache2-mod-auth-openidc \
            vim apt-utils \
            less && \
    rm -rf /var/lib/apt/lists/* && \
    a2enmod auth_openidc && \
    a2enmod headers && \
    a2enmod proxy && \
    a2enmod proxy_http && \
    a2enmod rewrite && \
#    a2enmod ssl && \
    a2enmod env && \
    a2enmod mime && \
    a2enmod proxy_wstunnel && \
    a2enmod negotiation
#    sed -i 's/#Mutex/Mutex/g' /etc/apache2/mods-enabled/ssl.conf

COPY apache2.conf /etc/apache2/apache2.conf
COPY site-marks.conf /etc/apache2/sites-enabled/site-marks.conf
#ADD ssl.conf /etc/apache2/mods-enabled/ssl.conf

RUN mkdir -p /var/run/apache2 /var/www/html/app && \
    mkdir -p /var/lock/apache2/ssl_mutex && \
    mkdir -p /config/global.d && \
    mkdir -p /config/vhost-80.d && \
    mkdir -p /config/vhost-443.d && \
    mkdir -p /var/run/apache2/mod_auth_openidc/cache && \
    chmod -R 755 /var/www/html/app /var/lock/apache2 && \
    chown -R www-data /var/run/apache2 /var/log/apache2 /var/www/html/app /var/lock/apache2 && \
    rm /etc/apache2/sites-enabled/000-default.conf

COPY dist /usr/local/apache2/htdocs
# https://httpd.apache.org/docs/2.4/stopping.html#gracefulstop
STOPSIGNAL WINCH

COPY httpd-foreground /usr/local/bin/

EXPOSE 80
CMD ["httpd-foreground"]