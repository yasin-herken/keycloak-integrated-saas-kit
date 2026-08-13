Act as a DevOps and QA Engineer.

[STRICT CONSTRAINTS & CONFIGURATION]
1. ZERO MANUAL INTERVENTION: Absolutely no manual UI or configuration steps. Everything must be handled via `deploy.sh`, `saas-init.yml`, and `realm-export.json.template`.
2. SAFE ISOLATED CLEANUP: DO NOT use `docker system prune` or global Docker commands. Only teardown containers, networks, and volumes belonging to THIS specific project.
3. PORT CONFIGURATION:
    - Keycloak runs in Docker on port 8080.
    - Spring Boot runs locally (outside Docker) on port 8081.

[OBJECTIVE]
Safely reset this project's infrastructure, deploy Keycloak (8080) via `deploy.sh`, run Spring Boot locally on port 8081, verify all endpoints, and double-check that the process is 100% automated and repeatable.

[STEPS TO EXECUTE]

STEP 1: Isolated Project Cleanup
- Run project-specific teardown: `docker compose -f infrastructure/docker-compose.yml down -v`
- Clean local build artifacts: `./mvnw clean`

STEP 2: Deploy Infrastructure
- Run `./deploy.sh`
- Verify via Keycloak logs (`docker compose -f infrastructure/docker-compose.yml logs -f keycloak`) that JWE SPI loaded and realm auto-imported successfully on port 8080.

STEP 3: Run Spring Boot Locally (Port 8081)
- Ensure `application.yml` sets `server.port=8081`.
- Run Spring Boot locally: `./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"`

STEP 4: Verification (cURL Tests against Port 8081)
1. Public Endpoint Test:
   `curl -X GET http://localhost:8081/api/v1/public/ping` -> Expect HTTP 200.
2. Authenticated JWE Test:
   Fetch JWE token from Keycloak (http://localhost:8080/realms/saas-realm/protocol/openid-connect/token) and test:
   `curl -X GET http://localhost:8081/api/v1/users/me -H "Authorization: Bearer <TOKEN>"` -> Expect JWE decoded user info.

STEP 5: Repeatability Verification
- Stop Spring Boot, run `docker compose -f infrastructure/docker-compose.yml down -v`, re-run `./deploy.sh` and confirm the entire flow works repeatedly with ZERO manual intervention.

[OUTPUT FORMAT]
Provide clear terminal commands for each step. If any manual setup step is required, provide the exact patch for `deploy.sh` or `realm-export.json.template` instead.