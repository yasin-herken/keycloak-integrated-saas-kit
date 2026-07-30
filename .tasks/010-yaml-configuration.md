Act as a Senior DevOps and Platform Engineer building an Enterprise SaaS Boilerplate.
Our stack includes Java 25, Spring Boot 4, Keycloak 26, and PostgreSQL. We strictly follow 12-Factor App principles and a "Zero-Touch" deployment philosophy.

[CURRENT STATE]
Currently, our `deploy.sh` script uses hardcoded `sed` commands and static directory paths (e.g., `infrastructure/keycloak/templates`) to generate configuration files from templates. This is rigid and violates our extensibility goal.

[OBJECTIVE]
Refactor the deployment architecture to be a dynamic, flexible "Templating Engine". Developers using this kit should be able to add new `.template` files ANYWHERE in the repository (e.g., `infrastructure/database`, `infrastructure/nginx`) and add new variables to `saas-init.yml` WITHOUT ever modifying `deploy.sh`.

[REQUIREMENTS]
1. Master Configuration: Create a sample `saas-init.yml` at the project root containing nested configurations (project name, keycloak credentials, security keys like JWE base64).
2. Dynamic Environment Variables: In `deploy.sh`, use `yq` to parse `saas-init.yml`. Dynamically flatten and convert all YAML keys into UPPERCASE environment variables (e.g., `project.name` -> `PROJECT_NAME`).
3. Dynamic Template Discovery: Use the `find` command in `deploy.sh` to recursively search for any file ending with `.template` within the `infrastructure/` directory (or other specified base directories).
4. Template Processing: Replace the hardcoded `sed` logic with `envsubst`. For each found `.template` file, generate the actual file in the same directory (or a target directory if specified) by stripping the `.template` extension, injecting the environment variables automatically.
5. Error Handling: Add checks to ensure `yq` and `envsubst` are installed. Fail fast (`set -e`) if something goes wrong.

[RULES & CONSTRAINTS]
- DO NOT use `sed` for variable substitution.
- DO NOT hardcode variable names inside `deploy.sh`.
- Keep the script clean, well-commented, and professional.
- Ensure the output enables a true "Zero-Touch" experience where the user only edits the YAML and runs the script to start `docker compose up -d`.

Please provide the updated `deploy.sh`, a sample `saas-init.yml`, and a brief explanation of how to structure a sample `.template` file (e.g., `realm-export.json.template`).