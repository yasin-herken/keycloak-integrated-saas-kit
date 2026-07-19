---
id: 003.1
title: Refactor Liquibase from SQL to XML Format
agent: spring-agent
status: pending
---

## Task Description
The architectural decision for database migrations has changed. We are moving away from pure SQL changelogs to XML formatted changelogs for Liquibase. Refactor the existing Liquibase setup in the `archcore-core` module to strictly use XML.

## Hard Constraints
* **Liquibase Format:** XML format ONLY (`.xml`). Do not use YAML or SQL for changesets.
* **Database Target:** PostgreSQL 18 (`archcore_db`).

## Requirements
1. Delete the existing `.sql` changeset file (e.g., `001-init-schema.sql`).
2. Update the master changelog file (e.g., `db.changelog-master.xml`) to use standard Liquibase XML namespaces and include the new XML changesets.
3. Create a new changeset file at `db/changelog/changesets/001-init-schema.xml`.
4. Inside `001-init-schema.xml`, use standard Liquibase XML tags (`<changeSet>`, `<createTable>`, `<column>`) to create the initial test table (e.g., `dummy_audit`), replacing the old SQL logic. Ensure the XML schema definitions at the top of the file are valid and point to the correct Liquibase XSD version.
5. Ensure `application.yml` correctly points to the XML master changelog (if it was previously pointing directly to a SQL file).

## Acceptance Criteria
* The application starts without errors and Liquibase executes successfully using the XML changeset.
* No `.sql` changeset files remain in the `db/changelog` directory.
* The test table is successfully created in `archcore_db` via the XML `<createTable>` instruction.