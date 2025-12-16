FROM ubuntu:22.04

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
            cron postgresql-client curl sudo

RUN adduser --disabled-password --gecos '' --uid 1939 backup-user && \
    adduser backup-user sudo

RUN echo 'backup-user ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers

ADD backup.sh /backup.sh
ADD start.sh /start.sh

USER backup-user

RUN sudo chmod 777 /backup.sh
RUN sudo chmod 777 /start.sh

CMD /start.sh