#!/usr/bin/env bash

set -euo pipefail

DB_APP_DB=koutaexternal
DB_APP_USER=oph

echo "Creating pg_cron extension to database \"$DB_APP_DB\" and granting usage to role \"$DB_APP_USER\"…"

psql "${DB_APP_DB}" -c "CREATE EXTENSION pg_cron" \
                    -c "GRANT USAGE ON SCHEMA \"cron\" TO \"${DB_APP_USER}\""
