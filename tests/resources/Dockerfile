FROM citusdata/citus:latest

RUN apt update \
    && apt install -y --no-upgrade --no-install-recommends \
      postgresql-18-postgis-3

COPY 01-init.sql /docker-entrypoint-initdb.d/01-init.sql
COPY 02-schema.sql /docker-entrypoint-initdb/02-schema.sql
COPY 03-inf-contributions.sql.gz /docker-entrypoint-initdb/03-inf-contributions.sql.gz
