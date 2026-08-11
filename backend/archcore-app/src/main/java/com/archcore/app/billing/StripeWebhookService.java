package com.archcore.app.billing;

import com.archcore.core.service.BillingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Service
public class StripeWebhookService implements BillingWebhookService {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookService.class);

    private static final Set<String> SUPPORTED_EVENTS = Set.of(
            "payment.succeeded",
            "payment.failed",
            "invoice.paid",
            "invoice.payment_failed",
            "customer.subscription.created",
            "customer.subscription.updated",
            "customer.subscription.deleted"
    );

    private final BillingService billingService;

    public StripeWebhookService(BillingService billingService) {
        this.billingService = billingService;
    }

    @Override
    public void processWebhook(BillingWebhookEvent event) {
        log.info("Processing Stripe webhook event: {}", event.type());

        switch (event.type()) {
            case "payment.succeeded", "invoice.paid" -> handlePaymentSucceeded(event);
            case "payment.failed", "invoice.payment_failed" -> handlePaymentFailed(event);
            case "customer.subscription.created", "customer.subscription.updated" -> handleSubscriptionUpdated(event);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
            default -> log.debug("Unhandled Stripe event type: {}", event.type());
        }
    }

    @Override
    public boolean supportsEventType(String eventType) {
        return SUPPORTED_EVENTS.contains(eventType);
    }

    private void handlePaymentSucceeded(BillingWebhookEvent event) {
        String subscriptionId = event.getExternalSubscriptionId();
        if (subscriptionId != null) {
            Instant nextBillingDate = Instant.now().plus(30, ChronoUnit.DAYS);
            billingService.handlePaymentSucceeded(subscriptionId, nextBillingDate);
            log.info("Payment succeeded for subscription: {}", subscriptionId);
        }
    }

    private void handlePaymentFailed(BillingWebhookEvent event) {
        String subscriptionId = event.getExternalSubscriptionId();
        if (subscriptionId != null) {
            log.warn("Payment failed for subscription: {}", subscriptionId);
        }
    }

    private void handleSubscriptionUpdated(BillingWebhookEvent event) {
        String subscriptionId = event.getExternalSubscriptionId();
        if (subscriptionId != null) {
            log.info("Subscription updated: {}", subscriptionId);
        }
    }

    private void handleSubscriptionDeleted(BillingWebhookEvent event) {
        String subscriptionId = event.getExternalSubscriptionId();
        if (subscriptionId != null) {
            billingService.handleSubscriptionDeleted(subscriptionId);
            log.info("Subscription deleted: {}", subscriptionId);
        }
    }
}
