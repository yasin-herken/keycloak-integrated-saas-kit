package com.archcore.app.billing.stripe;

import com.archcore.app.billing.BillingWebhookEvent;
import com.archcore.app.billing.BillingWebhookService;
import com.archcore.core.billing.PaymentGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Stripe implementation of the {@link PaymentGateway} interface.
 *
 * <p>This bean is conditionally loaded only when the billing provider is configured as
 * {@code stripe} in the application properties:</p>
 * <pre>
 * features:
 *   billing:
 *     provider: stripe
 * </pre>
 *
 * <p>In test mode, this implementation uses placeholder URLs and simplified webhook
 * processing. Replace with the official Stripe SDK dependency for production use.</p>
 *
 * <p>To add a new payment provider, create a similar class implementing
 * {@link PaymentGateway} with {@code @ConditionalOnProperty(havingValue = "your-provider")}.</p>
 */
@Service
@ConditionalOnProperty(prefix = "features.billing", name = "provider", havingValue = "stripe")
public class StripePaymentGatewayImpl implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentGatewayImpl.class);

    private static final String CHECKOUT_URL_TEMPLATE = "https://checkout.stripe.com/pay?session=%s-%s";

    private final List<BillingWebhookService> webhookServices;
    private final ObjectMapper objectMapper;

    public StripePaymentGatewayImpl(List<BillingWebhookService> webhookServices, ObjectMapper objectMapper) {
        this.webhookServices = webhookServices;
        this.objectMapper = objectMapper;
    }

    @Override
    public String createCheckoutSession(String userId, String planId) {
        String checkoutUrl = String.format(CHECKOUT_URL_TEMPLATE, userId, planId);
        log.info("Created Stripe checkout session for user {} plan {}: {}", userId, planId, checkoutUrl);
        return checkoutUrl;
    }

    @Override
    public void handleWebhook(String payload, String signature) {
        log.info("Processing Stripe webhook with signature: {}", signature);

        try {
            BillingWebhookEvent event = objectMapper.readValue(payload, BillingWebhookEvent.class);

            webhookServices.stream()
                    .filter(service -> service.supportsEventType(event.type()))
                    .findFirst()
                    .ifPresent(service -> {
                        try {
                            service.processWebhook(event);
                        } catch (Exception e) {
                            log.error("Error processing Stripe webhook event {}: {}", event.type(), e.getMessage(), e);
                        }
                    });
        } catch (JacksonException e) {
            log.error("Failed to parse Stripe webhook payload: {}", e.getMessage(), e);
        }
    }
}
