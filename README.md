# ArchCore Enterprise SaaS Kit

> A production-ready Java SaaS boilerplate with JWE encryption, zero-touch infrastructure, built-in billing, rate limiting, and audit logs. From clone to running backend in 5 minutes.

---

## Features

| Feature | Description |
|---------|-------------|
| **JWE Token Encryption** | End-to-end encrypted JWT tokens via custom Keycloak SPI. Tokens are encrypted at the IdP and decrypted locally by the Resource Server — no network calls to Keycloak for validation. |
| **Zero-Touch Infrastructure** | Single `saas-init.yml` config file drives Docker, Keycloak realms, feature flags, and environment variables via `deploy.sh`. |
| **Built-in Billing & Subscriptions** | Stripe-ready subscription management with plan tiers, trial support, and webhook handling. |
| **Dynamic Rate Limiting** | Annotation-based rate limiting (`@RateLimit`) with per-user, per-IP, or per-endpoint scoping via Bucket4j. |
| **Audit Logging** | Declarative activity tracking (`@LogActivity`) with AOP-powered automatic persistence. |
| **Profile Management** | User profile CRUD with automatic Keycloak sync and account lifecycle (activate/deactivate). |
| **Global Exception Handling** | Consistent error responses across all endpoints with validation support. |
| **Modular Monolith** | Clean separation: `core` (business logic) vs. `app` (HTTP layer) vs. `security` (auth infrastructure). |

---

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| **Docker** | 24+ | Container runtime for Keycloak & PostgreSQL |
| **Java** | 25 (Temurin) | Backend compilation and execution |
| **Maven** | 3.9+ | Build tool (wrapper included) |
| **yq** | 4.x | YAML processing in deploy.sh |
| **envsubst** | — | Environment variable substitution for templates |
| **jq** | 1.7+ | JSON post-processing for Keycloak realm configs |

---

## Quick Start

### Step 1: Configure Your Project

```bash
cp saas-init.yml.example saas-init.yml
```

Edit `saas-init.yml` with your project settings:

```yaml
project:
  name: myproject

features:
  forgotPassword:
    enabled: true
  googleLogin:
    enabled: true
  smtp:
    enabled: true

db:
  name: keycloak
  user: keycloak
  password: changeMe123!

keycloak:
  admin:
    user: admin
    pass: changeMe123!
  smtp:
    host: smtp.gmail.com
    port: 587
    user: your-email@gmail.com
    password: your-app-password
    from: noreply@yourdomain.com
  google:
    clientId: your-google-client-id.apps.googleusercontent.com
    clientSecret: your-google-client-secret

frontend:
  url: http://localhost:3000
```

### Step 2: Deploy Infrastructure

```bash
chmod +x deploy.sh
./deploy.sh
```

This generates:
- `.env` — Docker environment variables
- `docker-compose.yml` — Container definitions
- `realm-dev.json` / `realm-prod.json` — Keycloak realm configurations

### Step 3: Start Services

```bash
docker compose up -d
```

### Step 4: Run the Backend

```bash
cd backend
./mvnw spring-boot:run -pl archcore-app
```

The API is now live at `http://localhost:8080`.

### Step 5: Verify

```bash
# Public health check (no auth required)
curl http://localhost:8080/api/v1/public/ping

# Expected response:
# {"status":"UP","timestamp":"2026-08-11T...","service":"ArchCore SaaS Kit"}
```

---

## Architecture

```
keycloak-integrated-saas-kit/
├── saas-init.yml                  # Single source of truth for all config
├── deploy.sh                      # Config generation engine
├── docker-compose.yml             # Generated container definitions
├── backend/
│   ├── pom.xml                    # Parent POM (multi-module)
│   ├── archcore-core/             # Business logic layer
│   │   └── src/main/java/com/archcore/core/
│   │       ├── domain/            # Entities (Subscription, Plan, UserProfile, AuditLog)
│   │       ├── repository/        # Spring Data JPA repositories
│   │       └── service/           # Business services (BillingService, UserProfileService)
│   ├── archcore-security/         # Authentication infrastructure
│   │   └── src/main/java/com/archcore/security/
│   │       ├── config/            # SecurityConfig, JwtDecoderConfig, JweProperties
│   │       └── converter/         # KeycloakJwtAuthenticationConverter
│   └── archcore-app/              # Application entry point (HTTP layer)
│       └── src/main/java/com/archcore/app/
│           ├── controller/        # REST controllers (Sample, User, Admin, Billing)
│           ├── ratelimit/         # @RateLimit annotation + Bucket4j filter
│           ├── audit/             # @LogActivity annotation + AOP aspect
│           ├── exception/         # GlobalExceptionHandler + ErrorResponse
│           ├── dto/               # Request/Response records
│           └── filter/            # RateLimitFilter
├── keycloak-jwe-spi/              # Custom Keycloak SPI for JWE token encryption
│   └── src/main/java/com/archcore/keycloak/spi/jwe/
│       ├── JweAccessTokenResponseMapper.java
│       └── JwksClient.java
└── infrastructure/
    └── keycloak/
        ├── Dockerfile             # Custom Keycloak image with JWE SPI
        └── templates/             # Realm JSON templates (dev/prod)
```

### Core vs. Domain Isolation

| Layer | Module | Responsibility |
|-------|--------|----------------|
| **Core** | `archcore-core` | Entities, repositories, business services. No HTTP, no annotations. |
| **Security** | `archcore-security` | JWT/JWE validation, Keycloak integration, security filter chain. |
| **App (Domain)** | `archcore-app` | REST controllers, DTOs, AOP aspects, filters. Depends on core + security. |

**Rule:** Domain controllers use core services. Core never imports app or security.

---

## API Authentication Flow

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│    Client     │         │   Keycloak   │         │  Backend API │
│              │         │  (IdP + JWE) │         │  (Resource   │
│              │         │              │         │   Server)    │
└──────┬───────┘         └──────┬───────┘         └──────┬───────┘
       │                        │                        │
       │  1. POST /auth/token   │                        │
       │  (username + password) │                        │
       │───────────────────────>│                        │
       │                        │                        │
       │  2. JWE-encrypted JWT  │                        │
       │  (sub, email, roles)   │                        │
       │<───────────────────────│                        │
       │                        │                        │
       │  3. GET /api/v1/users/me                       │
       │  Authorization: Bearer <JWE>                   │
       │───────────────────────────────────────────────>│
       │                        │                        │
       │                        │  4. Decrypt JWE locally│
       │                        │  using RSA private key │
       │                        │  (no network call)     │
       │                        │                        │
       │  5. 200 OK + response  │                        │
       │<───────────────────────────────────────────────│
```

**Key Point:** The backend validates tokens **locally** using the RSA key pair generated during `deploy.sh`. No network call to Keycloak is needed for token validation — this is what makes the JWE SPI approach both secure and fast.

---

## How to Extend the Domain

Creating a new endpoint takes ~5 lines:

```java
@RestController
@RequestMapping("/api/v1/my-domain")
public class MyDomainController {

    @GetMapping("/data")
    @RateLimit(requests = 20, periodSeconds = 60, scope = RateLimit.RateLimitScope.USER)
    @LogActivity(description = "Fetched domain data")
    public ResponseEntity<Map<String, Object>> getData(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(Map.of("userId", jwt.getSubject(), "data", "hello"));
    }
}
```

That's it. You get:
- **Authentication** — via `@AuthenticationPrincipal Jwt jwt` (auto-validated JWE token)
- **Rate Limiting** — via `@RateLimit` (Bucket4j, per-user)
- **Audit Logging** — via `@LogActivity` (auto-persisted to `audit_log` table)
- **Role Protection** — via `@PreAuthorize("hasRole('ADMIN')")` when needed

### Available Annotations

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@RateLimit` | Throttle requests per user/IP/endpoint | `@RateLimit(requests = 10, periodSeconds = 60)` |
| `@LogActivity` | Automatic audit trail | `@LogActivity(description = "User updated profile")` |
| `@PreAuthorize` | Role-based access control | `@PreAuthorize("hasRole('ADMIN')")` |
| `@SubscriptionRequired` | Feature gating by plan tier | Coming soon |

---

## Common Commands

```bash
# Infrastructure
docker compose up -d              # Start Keycloak + PostgreSQL
docker compose down               # Stop containers
docker compose down -v            # Stop + delete all data
docker compose restart keycloak   # Restart Keycloak only

# Backend
cd backend
./mvnw spring-boot:run -pl archcore-app    # Run the app
./mvnw clean install                        # Build all modules
./mvnw test                                 # Run tests

# Config Regeneration
./deploy.sh                                 # Regenerate from saas-init.yml
./deploy.sh && docker compose up -d         # Regenerate + restart
```

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| **Port 8080 in use** | Change `server.port` in `application.yml` or stop the conflicting process |
| **Port 5433 conflict** | Edit `docker-compose.yml` to map to a different host port |
| **Keycloak won't start** | Check logs: `docker logs archcore-keycloak` |
| **Token validation fails** | Ensure Keycloak is running and `jwks.json` is generated in project root |
| **Missing features** | Re-run `./deploy.sh` after editing `saas-init.yml`, then restart Keycloak |

---

## License

MIT
