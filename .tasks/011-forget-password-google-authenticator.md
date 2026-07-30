Act as a Principal IAM Architect specializing in Keycloak 26.

[CONTEXT]
I already have a fully automated "Zero-Touch" deployment pipeline. My custom setup (`saas-init.yml` -> `deploy.sh` -> `docker-compose`) dynamically injects all necessary environment variables into the Keycloak container on startup.

[OBJECTIVE]
I need the exact JSON configuration snippets for a `realm-export.json` file. When Keycloak boots and runs `--import-realm`, this JSON must automatically configure "Forgot Password", "Google Social Login", and a base SPA client, strictly by reading the environment variables my system injects. No manual UI configuration should be required by the end-developer.

[REQUIREMENTS]
Generate the JSON configurations for the following features using Keycloak's variable substitution syntax (e.g., `${env.SMTP_HOST}`):

1. Realm Settings:
- Enable `resetPasswordAllowed` (Forgot Password).
- Enable `registrationAllowed` (if applicable for SaaS).

2. SMTP Configuration (For Forgot Password emails):
- Provide the JSON block to configure the SMTP server dynamically using: `${env.KC_SMTP_HOST}`, `${env.KC_SMTP_PORT}`, `${env.KC_SMTP_USER}`, `${env.KC_SMTP_PASSWORD}`, and `${env.KC_SMTP_FROM}`.

3. Identity Provider (Google Login):
- Provide the JSON block to configure Google Social Login dynamically using: `${env.KC_GOOGLE_CLIENT_ID}` and `${env.KC_GOOGLE_CLIENT_SECRET}`.

4. Client Configuration:
- A standard public client (e.g., `saas-frontend`) configured for standard flow and standard web origins (`*` or `${env.FRONTEND_URL}`).

[RULES]
Do not write Docker Compose or shell scripts. ONLY provide the internal structure and necessary JSON blocks for the Keycloak realm export file, explaining exactly where they go in a standard Keycloak realm JSON structure.