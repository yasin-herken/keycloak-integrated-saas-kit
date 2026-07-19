---
id: 001
title: Dynamic Keycloak and PostgreSQL Infrastructure Setup
agent: infra-agent
status: pending
---

## Task Description
A `docker-compose.yml` will be created for local and production environments. The infrastructure will be set up using the latest versions (PostgreSQL 17 and Keycloak latest), and critical project variables will be read dynamically from the `.env` file. The production realm name will not have a postfix — it will use only the project name.

## Requirements
1. A sample `.env.example` file will be created in the root directory. It will contain variables for `PROJECT_NAME`, database passwords, and Keycloak admin credentials.
2. A PostgreSQL (v17 image) container will be added. Passwords and database info will be read from the `.env` file.
3. A Keycloak (latest image) container will be added. It will connect to the PostgreSQL 17 container as its database.
4. A `./infrastructure/keycloak/templates/` folder will be created in the project directory.
5. Two template files will be added to this folder:
   * `realm-dev.json.template` (Realm name: set to `${PROJECT_NAME}-dev`)
   * `realm-prod.json.template` (Realm name: set to `${PROJECT_NAME}` only — no postfix)
6. The Keycloak service in `docker-compose.yml` will read these `.template` files before container startup, substitute the `PROJECT_NAME` variable from `.env` using `envsubst`, convert them to actual `.json` files, and import them into the system.

## Acceptance Criteria
* When the user sets `PROJECT_NAME=archcore` in the `.env` file, the system should start without errors.
* When accessing the Keycloak panel, realm names should appear as `archcore-dev` for development and `archcore` for production.
* Image versions should be PostgreSQL 17 and Keycloak's latest major version in the configuration.
* Files should contain no hardcoded names or passwords.
