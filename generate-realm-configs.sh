#!/bin/sh
set -e

# Load environment variables from .env
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
fi

TEMPLATE_DIR="infrastructure/keycloak/templates"
IMPORT_DIR="infrastructure/keycloak/import"

mkdir -p "$IMPORT_DIR"

for template in "$TEMPLATE_DIR"/*.json.template; do
  filename=$(basename "$template" .json.template)
  output="$IMPORT_DIR/$filename.json"

  # Use envsubst-style replacement with sed
  sed \
    -e "s|\${PROJECT_NAME}|${PROJECT_NAME}|g" \
    -e "s|\${KEYCLOAK_ADMIN_USER}|${KEYCLOAK_ADMIN_USER}|g" \
    -e "s|\${KEYCLOAK_ADMIN_PASS}|${KEYCLOAK_ADMIN_PASS}|g" \
    "$template" > "$output"

  echo "Generated: $output"
done
