# Enterprise SaaS Kit - Architecture

## Quick Reference
- **Stack:** Java 25, Spring Boot 4, Keycloak 26+, PostgreSQL 18
- **Pattern:** Modular Monolith, 12-Factor App, Stateless REST API
- **Auth:** Keycloak handles ALL auth. JWE SPI for token encryption. Backend = Resource Server only.
- **Config:** `saas-init.yml` → `deploy.sh` → `.env` + `docker-compose.yml` → docker compose
- **Templates:** `.template` files in `infrastructure/` auto-discovered by `deploy.sh`
- **Features:** Optional features controlled via `features` section in `saas-init.yml`

## Key Rules
- No custom auth logic in backend
- Lombok mandatory for DTOs/Entities
- Package convention: `com.archcore.*`
- Frontend = dumb components (no optimization)
- Every new file → Git immediately

## Current State (2026-08-14)
- ✅ Keycloak + PostgreSQL Docker setup
- ✅ JWE SPI for token encryption (keycloak-jwe-spi module)
- ✅ Templating engine (deploy.sh)
- ✅ Feature flags system (forgotPassword, googleLogin, smtp)
- ✅ Dynamic docker-compose.yml generation
- ✅ Realm JSON post-processing with jq
- ✅ Backend scaffold (Spring Boot 4 + Java 25) — Multi-module complete
- ✅ Security module — JWE validation, Keycloak integration, SecurityConfig
- ✅ Core module — Domain entities (Subscription, Plan, UserProfile, AuditLog), repositories, services
- ✅ App module — REST controllers, DTOs, AOP aspects, filters
- ✅ Rate limiting — Bucket4j + @RateLimit annotation
- ✅ Audit logging — @LogActivity + AOP aspect
- ✅ Global exception handling — GlobalExceptionHandler + ErrorResponse
- ✅ User profile management — CRUD + Keycloak sync
- ✅ Billing — Stripe webhook handling
- ✅ User registration & account deletion
- ✅ Frontend — Next.js 15 with SSO integration
  - Landing page with hero, features, pricing, about, CTA sections
  - Sign In/Sign Up pages with Keycloak + Google SSO
  - Dashboard for authenticated users
  - Middleware for route protection
  - Tailwind CSS styling
- ⏳ Redis cache (planned)
- ⏳ Kubernetes manifests (planned)

## Detailed Documentation
- `arch/auth.md` — Authentication & JWE SPI details
- `arch/infra.md` — Docker & infrastructure
- `arch/config.md` — Configuration flow & templating
