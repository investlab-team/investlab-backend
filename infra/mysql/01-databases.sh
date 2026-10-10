#!/bin/bash
set -euo pipefail
[[ "$CORE_DB_PASSWORD" =~ ^[a-zA-Z0-9]{16,}$ ]] || { echo 'Use an alphanumeric core password of at least 16 characters'; exit 1; }
[[ "$RESEARCH_DB_PASSWORD" =~ ^[a-zA-Z0-9]{16,}$ ]] || { echo 'Use an alphanumeric research password of at least 16 characters'; exit 1; }
mysql -uroot -p"$MYSQL_ROOT_PASSWORD" <<SQL
CREATE DATABASE IF NOT EXISTS investlab_core CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS investlab_research CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'investlab_core'@'%' IDENTIFIED BY '${CORE_DB_PASSWORD}';
CREATE USER IF NOT EXISTS 'investlab_research'@'%' IDENTIFIED BY '${RESEARCH_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON investlab_core.* TO 'investlab_core'@'%';
GRANT ALL PRIVILEGES ON investlab_research.* TO 'investlab_research'@'%';
SQL
