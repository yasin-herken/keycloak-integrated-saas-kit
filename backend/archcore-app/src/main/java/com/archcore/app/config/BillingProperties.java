package com.archcore.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the pluggable billing system.
 *
 * <p>Controls whether billing is enabled and which payment provider is active.
 * The provider value determines which {@link com.archcore.core.billing.PaymentGateway}
 * implementation is loaded into the Spring context.</p>
 *
 * <p>Example configuration in {@code application.yml}:</p>
 * <pre>
 * features:
 *   billing:
 *     enabled: true
 *     provider: stripe
 * </pre>
 *
 * <p>To add a new payment provider (e.g., LemonSqueezy), create a class that
 * implements {@code PaymentGateway}, annotate it with {@code @Service} and
 * {@code @ConditionalOnProperty(prefix = "features.billing", name = "provider", havingValue = "lemonsqueezy")},
 * and set {@code features.billing.provider=lemonsqueezy} in your configuration.</p>
 */
@ConfigurationProperties(prefix = "features.billing")
public record BillingProperties(
    boolean enabled,
    String provider
) {
    public BillingProperties {
        provider = provider != null ? provider : "none";
    }

    public static final String PROVIDER_STRIPE = "stripe";
    public static final String PROVIDER_NONE = "none";
}
