package com.archcore.core.billing;

/**
 * Core abstraction for payment provider integrations.
 *
 * <p>This interface defines the contract that all payment provider implementations must fulfill.
 * The application depends on this abstraction, not on concrete provider classes, following
 * the Dependency Inversion Principle (SOLID).</p>
 *
 * <h3>Adding a New Provider</h3>
 * <p>To integrate a new payment provider (e.g., LemonSqueezy, Paddle, Iyzico):</p>
 * <ol>
 *   <li>Create a class implementing this interface in the {@code archcore-app} module</li>
 *   <li>Annotate it with {@code @Service} and
 *       {@code @ConditionalOnProperty(prefix = "features.billing", name = "provider", havingValue = "your-provider")}</li>
 *   <li>Implement {@link #createCheckoutSession} to generate a provider-specific checkout URL</li>
 *   <li>Implement {@link #handleWebhook} to process provider-specific webhook payloads</li>
 *   <li>Set {@code features.billing.provider=your-provider} in your configuration</li>
 * </ol>
 *
 * <p>The Spring context will automatically load only the implementation matching the configured
 * provider, ensuring zero coupling between providers at runtime.</p>
 */
public interface PaymentGateway {

    /**
     * Creates a checkout session for the given user and plan.
     *
     * @param userId the unique identifier of the user initiating checkout
     * @param planId the identifier of the plan to subscribe to
     * @return the checkout URL where the user should be redirected to complete payment
     */
    String createCheckoutSession(String userId, String planId);

    /**
     * Processes an incoming webhook payload from the payment provider.
     *
     * <p>The implementation is responsible for:</p>
     * <ul>
     *   <li>Validating the webhook signature to ensure authenticity</li>
     *   <li>Parsing the provider-specific payload format</li>
     *   <li>Delegating to the appropriate business logic (e.g., subscription updates)</li>
     * </ul>
     *
     * @param payload  the raw JSON webhook body
     * @param signature the provider-specific signature header for validation
     */
    void handleWebhook(String payload, String signature);
}
