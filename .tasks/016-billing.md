Act as a Principal Software Architect.

[CONTEXT]
We are building a "Zero-Touch Enterprise SaaS Boilerplate" using Spring Boot. We need a pluggable, scalable billing architecture. We CANNOT hardcode Stripe. The end-buyer might want to use LemonSqueezy, Paddle, Iyzico, or disable billing entirely via our `saas-init.yml` configuration.

[OBJECTIVE]
Design and implement a Pluggable Billing Architecture using the Strategy/Adapter pattern. The core system must depend on abstractions, not concrete Stripe classes.

[REQUIRED DELIVERABLES]

TASK 1: Configuration & Feature Flags (`application.yml` mapping)
- Create a configuration properties record/class to map:
  `features.billing.enabled` (boolean)
  `features.billing.provider` (String, e.g., "stripe", "none")

TASK 2: Core Abstraction (The Interface)
- Create a `PaymentGateway` interface in the `core` layer with methods:
    1. `String createCheckoutSession(String userId, String planId)`
    2. `void handleWebhook(String payload, String signature)`

TASK 3: Stripe Implementation & Conditional Loading
- Create `StripePaymentGatewayImpl` implementing `PaymentGateway`.
- CRITICAL: Use Spring's `@ConditionalOnProperty(prefix = "features.billing", name = "provider", havingValue = "stripe")` so this bean is ONLY loaded if Stripe is selected in the config.
- Implement the test-mode Stripe logic (creating a checkout URL and verifying the webhook).

TASK 4: The Controller Layer
- Create `BillingController`.
- Inject the `PaymentGateway` interface (not the Stripe implementation).
- Use `@ConditionalOnProperty(prefix = "features.billing", name = "enabled", havingValue = "true")` on the controller so the endpoints simply disappear (return 404) if the developer disables billing entirely in `saas-init.yml`.

[CONSTRAINTS]
- Adhere to SOLID principles (specifically Dependency Inversion).
- Add clear JavaDoc comments explaining to the boilerplate buyer how they can add a new provider (e.g., `LemonSqueezyPaymentGatewayImpl`) just by implementing the interface and adding a new `@ConditionalOnProperty`.