You are a Senior "Enterprise Java & SaaS Platform Architect".
Task: Enhance our "Zero-Touch SaaS Boilerplate" project with 4 essential modules required for any production-ready SaaS application: Subscriptions & Billing, User Profile Management, API Rate Limiting, and Audit Logging.

[ARCHITECTURAL CONTEXT & CONSTRAINTS]
- Tech Stack: Java 25, Spring Boot 4.x (using current Spring Security Lambda DSL), PostgreSQL, JPA/Hibernate.
- Design Philosophy: Strict separation between "Core" (Infrastructure/System) and "Domain" (Business Logic).
- Package Agnostic: DO NOT use specific `package com...` declarations in code samples. Modules should be flexible and decoupled.
- Zero-Touch & DX: The developer adopting this kit must not manually configure database tables or complex security chains. Everything must work seamlessly out of the box using clean JPA entities, AOP (Aspects), and filters.

Please implement the following 4 modules in order, with clean, modern, and production-ready code examples:

MODULE 1: Subscriptions & Billing Infrastructure
- Flexible Data Model: Create `Subscription` and `Plan` JPA Entities. Include plan tiers (`FREE`, `PRO`, `ENTERPRISE`), subscription statuses (`ACTIVE`, `CANCELED`, `PAST_DUE`), and billing cycle expiration dates. Extend from a `BaseEntity` (UUID id, createdAt, updatedAt).
- Webhook Handler Infrastructure: Create a generic `BillingWebhookService` interface and skeleton to process incoming webhook events from payment providers like Stripe or Iyzico (e.g., `payment.succeeded`, `subscription.deleted`).

MODULE 2: User Profile & Account Management
- Profile Updates: Write a REST Controller and Service layer to allow authenticated users to update their personal information (e.g., first name, last name, profile picture URL).
- Account Deletion (GDPR/Compliance): Implement a secure endpoint allowing users to initiate self-service account deletion or deactivation.

MODULE 3: API Rate Limiting & Quota Management
- Spring Security Filter / HandlerInterceptor: Create a lightweight rate-limiting mechanism based on client IP address or Authenticated User ID.
- Dynamic Plan-Based Limits: Implement a custom `@RateLimit` annotation and AOP/Filter component capable of enforcing different limits based on user subscription tiers (e.g., 60 requests/min for FREE, 1000 requests/min for PRO). Return HTTP 429 Too Many Requests upon breach.

MODULE 4: Audit Logging & Activity History
- `AuditLog` Entity: Create an entity to store User ID, accessed endpoint, HTTP method, client IP address, timestamp, and HTTP response status.
- `@LogActivity` Annotation: Implement an AOP Aspect that triggers asynchronously (`@Async`) when a developer decorates a controller method with `@LogActivity(description = "Project Created")`, logging the action to the database without blocking the main request thread.

MODULE 5: Integration & Usage Example
- Provide a clean sample REST Controller showing how an end-developer using the `domain` layer will seamlessly utilize these core modules together.