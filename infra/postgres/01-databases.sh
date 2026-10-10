#!/bin/bash
set -euo pipefail
# psql literal quoting handles passwords without SQL interpolation.
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
  --set=core_password="$CORE_DB_PASSWORD" --set=research_password="$RESEARCH_DB_PASSWORD" <<'SQL'
CREATE ROLE investlab_core LOGIN PASSWORD :'core_password';
CREATE ROLE investlab_research LOGIN PASSWORD :'research_password';
CREATE DATABASE investlab_core OWNER investlab_core ENCODING 'UTF8' TEMPLATE template0;
CREATE DATABASE investlab_research OWNER investlab_research ENCODING 'UTF8' TEMPLATE template0;
REVOKE CONNECT ON DATABASE investlab_core FROM PUBLIC;
REVOKE CONNECT ON DATABASE investlab_research FROM PUBLIC;
GRANT CONNECT ON DATABASE investlab_core TO investlab_core;
GRANT CONNECT ON DATABASE investlab_research TO investlab_research;
ALTER ROLE investlab_core SET timezone = 'UTC';
ALTER ROLE investlab_research SET timezone = 'UTC';
SQL
