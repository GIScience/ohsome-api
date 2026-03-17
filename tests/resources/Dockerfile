# change to trixie once citus repos are available
#FROM postgres:17-bookworm
FROM citusdata/citus:latest

# install base dependencies
#RUN apt update \
#    && apt install -y --no-upgrade --no-install-recommends \
#      ca-certificates \
#      curl



# add citus repository
#RUN curl -L https://packagecloud.io/citusdata/community/gpgkey -o /usr/share/keyrings/citus.key
#RUN echo "deb [signed-by=/usr/share/keyrings/citus.key] https://packagecloud.io/citusdata/community/debian bookworm main" > /etc/apt/sources.list.d/citus.list

# install citus
RUN apt update \
    && apt install -y --no-upgrade --no-install-recommends \
      postgresql-18-postgis-3
