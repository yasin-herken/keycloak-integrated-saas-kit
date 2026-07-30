# Enterprise SaaS Kit - Architecture

## Quick Reference
- **Stack:** Java 25, Spring Boot 4, Keycloak 26+, PostgreSQL 18
- **Pattern:** Modular Monolith, 12-Factor App, Stateless REST API
- **Auth:** Keycloak handles ALL auth. JWE SPI for token encryption. Backend = Resource Server only.
- **Config:** `saas-init.yml` → `deploy.sh` → `.env` → docker compose
- **Templates:** `.template` files in `infrastructure/` auto-discovered by `deploy.sh`

## Key Rules
- No custom auth logic in backend
- Lombok mandatory for DTOs/Entities
- Package convention: `com.archcore.*`
- Frontend = dumb components (no optimization)
- Every new file → Git immediately

## Current State (2026-07-30)
- ✅ Keycloak + PostgreSQL Docker setup
- ✅ JWE SPI for token encryption
- ✅ Templating engine (deploy.sh)
- ⏳ Backend scaffold (Spring Boot 4 + Java 25)

## Detailed Documentation
- `arch/auth.md` — Authentication & JWE SPI details
- `arch/infra.md` — Docker & infrastructure
- `arch/config.md` — Configuration flow & templating
