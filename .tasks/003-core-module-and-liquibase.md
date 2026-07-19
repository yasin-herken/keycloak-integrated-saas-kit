---
id: 003
title: Core Module Initialization and Liquibase Database Migration
agent: codegen-agent
status: pending
---

## Task Description
Initialize the `archcore-core` module, which will house the central domain logic, base entities, and database migration configurations. Integrate Liquibase to manage database schema versioning.

## Hard Constraints
* **Database Target:** PostgreSQL 18 (use context7)
* **Migration Tool:** Liquibase
* **Java Version:** 25
* **Spring Boot Version:** 4.x

## Requirements
1. Initialize the `backend/archcore-core` module.
2. Add Liquibase and Spring Data JPA dependencies to `archcore-core`.
3. Add `archcore-core` as a dependency inside the main executable module (`archcore-app`).
4. Set up the Liquibase directory structure within `archcore-core/src/main/resources/db/changelog/`.
5. Create the root changelog file (`db.changelog-master`).
6. Update `application.yml` in `archcore-app` to enable Liquibase and define the changelog path.
7. Ensure the configured PostgreSQL driver is fully compatible with PostgreSQL 18.

## Acceptance Criteria
* The application successfully compiles and starts.
* Upon startup, Liquibase automatically connects to the PostgreSQL 18 database and generates its standard tracking tables (`databasechangelog` and `databasechangeloglock`).
* The multi-module dependency chain (`archcore-app` -> `archcore-core`) functions without circular dependency errors.