package com.archcore.app.controller;

import com.archcore.core.billing.PaymentGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for provider-agnostic billing operations.
 *
 * <p>This controller is conditionally loaded based on the billing feature flag.
 * When {@code features.billing.enabled=false}, the bean is not created and all
 * endpoints return HTTP 404.</p>
 *
 * <p>The controller delegates all payment operations to the {@link PaymentGateway}
 * interface, which is resolved at runtime to the configured provider implementation
 * (e.g., Stripe, LemonSqueezy).</p>
 */
@RestController
@RequestMapping("/api/v1/billing")
@ConditionalOnProperty(prefix = "features.billing", name = "enabled", havingValue = "true")
public class BillingController {

    private static final Logger log = LoggerFactory.getLogger(BillingController.class);

    private final PaymentGateway paymentGateway;

    public BillingController(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    /**
     * Creates a checkout session and returns the payment URL.
     *
     * @param userId the authenticated user's ID
     * @param planId the plan to subscribe to
     * @return a map containing the checkout URL
     */
    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @RequestParam String userId,
            @RequestParam String planId) {

        log.info("Checkout request from user {} for plan {}", userId, planId);
        String checkoutUrl = paymentGateway.createCheckoutSession(userId, planId);
        return ResponseEntity.ok(Map.of("checkoutUrl", checkoutUrl));
    }

    /**
     * Receives and processes webhook events from the configured payment provider.
     *
     * @param payload   the raw webhook body
     * @param signature the provider-specific signature header
     * @return acknowledgement of receipt
     */
    @PostMapping("/webhook/payment")
    public ResponseEntity<Map<String, String>> handlePaymentWebhook(
            @RequestBody String payload,
            @RequestParam(required = false) String signature) {

        log.info("Received payment webhook");
        paymentGateway.handleWebhook(payload, signature);
        return ResponseEntity.ok(Map.of("status", "received"));
    }
}
