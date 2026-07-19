---
id: 002.1
title: Infrastructure Database Isolation (Running Instance Support)
agent: infra-agent
status: pending
---

## Task Description
Isolate the core application database from the IAM database. Since the PostgreSQL 18 container is already running and the volume contains existing Keycloak data, standard Docker entrypoint scripts cannot be used. The agent must create the `archcore_db` database dynamically on the running container without causing data loss to the existing Keycloak setup.

## Hard Constraints
* **Database Target:** PostgreSQL 18 (use context7)
* **Action:** Non-destructive. DO NOT drop or recreate the existing Docker volume.

## Requirements
1. Create a shell script named `scripts/create-archcore-db.sh` (or a similar utility file) in the project root.
2. The script must execute a command inside the running PostgreSQL container to create the `archcore_db` database idempotently (e.g., checking if it exists first, or ignoring the "already exists" error).
    * Example approach: `docker exec -i <postgres-container-name> psql -U <postgres-user> -tc "SELECT 1 FROM pg_database WHERE datname = 'archcore_db'" | grep -q 1 || docker exec -i <postgres-container-name> psql -U <postgres-user> -c "CREATE DATABASE archcore_db"`
3. Update the backend application environment variables (e.g., `.env` or `application.yml` for `archcore-app`) so that `SPRING_DATASOURCE_URL` points explicitly to the newly created `archcore_db`.

## Acceptance Criteria
* Executing the script creates the `archcore_db` database successfully.
* The existing Keycloak data and `keycloak_db` remain completely intact and functional.
* The Spring Boot backend can successfully connect to `archcore_db` without throwing "database does not exist" errors.