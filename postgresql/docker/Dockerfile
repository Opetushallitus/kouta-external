# Based on <https://hub.docker.com/_/postgres/>

FROM postgres:12.12

COPY ./configure-postgres.sh /docker-entrypoint-initdb.d/
COPY ./configure-koutaexternal-database.sh /docker-entrypoint-initdb.d/