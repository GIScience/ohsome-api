FROM postgres:17-bookworm

RUN apt update \
    && apt install -y --no-upgrade --no-install-recommends \
      postgresql-17-postgis-3

COPY initdb.d /docker-entrypoint-initdb.d
