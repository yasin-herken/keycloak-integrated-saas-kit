You are a Principal Java Developer and Technical Writer building the final delivery phase of an Enterprise SaaS Boilerplate.

[CONTEXT & ARCHITECTURAL SUMMARY]
We have successfully implemented:
- Phase 1: Zero-Touch Infrastructure (saas-init.yml, deploy.sh, Keycloak 26 + JWE SPI, Docker Compose).
- Phase 2: Core Security (Network-Free Local JWE Decoding, Role Converter, SecurityConfig).
- Phase 3: Core SaaS Engine (BaseEntity, GlobalExceptionHandler, Subscriptions/Stripe, Profile Management, Dynamic Rate Limiting, Audit Logging).

[OBJECTIVE]
Complete PHASE 4 by implementing a Showcase/Test Domain Controller and generating a world-class, developer-friendly `README.md` that guides the end-developer from repository clone to a running production-ready backend in 5 minutes.

[REQUIREMENTS]

TASK 16: Showcase Domain Controller & Integration Test Endpoint
- Package Agnostic: Do NOT use specific `package com...` declarations.
- Create a `TestDomainController` inside the `domain` layer to demonstrate how an end-developer uses our `core` features WITHOUT writing infrastructure code.
- Endpoint 1: `GET /api/v1/public/ping` -> Unauthenticated endpoint returning system status and current timestamp.
- Endpoint 2: `GET /api/v1/users/me` -> Authenticated endpoint extracting user details (user ID, email, roles) from the decoded JWE token using `@AuthenticationPrincipal Jwt jwt`.
- Endpoint 3: `GET /api/v1/admin/dashboard` -> Protected endpoint requiring `@PreAuthorize("hasRole('ADMIN')")`, applying `@RateLimit(requests = 10, timeWindowSeconds = 60)`, and logging activity via `@LogActivity(description = "Admin Dashboard Accessed")`.

TASK 17: Production-Ready README.md
- Generate a comprehensive, beautifully structured `README.md` file in Markdown.
- Sections required:
    1. Project Overview & Features Highlight (JWE Encryption, Zero-Touch Setup, Built-in Billing, Rate Limiting, Audit Logs).
    2. Prerequisites (Docker, Java 25, yq, envsubst).
    3. Quick Start Guide (Step 1: Edit `saas-init.yml`, Step 2: Run `./deploy.sh`, Step 3: Run Spring Boot).
    4. Architecture & Directory Structure (Explaining Core vs. Domain isolation).
    5. API Authentication Flow (How Keycloak issues JWE tokens and how the Resource Server validates them locally).
    6. How to Extend the Domain (Show a 5-line code snippet of how the developer can create their own Controller using `@SubscriptionRequired` or `@RateLimit`).

[CONSTRAINTS & QUALITY RULES]
- Keep code clean, modern (Java 25 features, Lombok), and fully functional.
- The `README.md` should read like a premium $199 SaaS Boilerplate documentation (e.g., ShipFast for Java).