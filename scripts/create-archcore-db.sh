#!/usr/bin/env bash
set -euo pipefail

DB_USER="${DB_USER:-keycloak}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5433}"
DB_NAME="archcore_db"

# Docker veya local psql seçimi
USE_DOCKER="${USE_DOCKER:-auto}"

run_psql() {
  if [ "${USE_DOCKER}" = "true" ] || { [ "${USE_DOCKER}" = "auto" ] && command -v docker &>/dev/null && docker ps --format '{{.Names}}' | grep -q "postgres"; }; then
    local container="${POSTGRES_CONTAINER:-archcore-postgres}"
    docker exec -i "${container}" psql -U "${DB_USER}" "$@"
  else
    psql -U "${DB_USER}" -h "${DB_HOST}" -p "${DB_PORT}" "$@"
  fi
}

echo "Checking if database '${DB_NAME}' exists..."

EXISTS=$(run_psql -tc "SELECT 1 FROM pg_database WHERE datname = '${DB_NAME}'" 2>/dev/null | tr -d '[:space:]')

if [ "${EXISTS}" = "1" ]; then
  echo "Database '${DB_NAME}' already exists. Skipping creation."
else
  echo "Creating database '${DB_NAME}'..."
  run_psql -c "CREATE DATABASE ${DB_NAME}"
  echo "Database '${DB_NAME}' created successfully."
fi

echo "Verifying database list:"
run_psql -tc "SELECT datname FROM pg_database WHERE datistemplate = false ORDER BY datname"
