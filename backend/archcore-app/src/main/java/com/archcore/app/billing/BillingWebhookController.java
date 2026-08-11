package com.archcore.app.billing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/billing/webhooks")
public class BillingWebhookController {

    private static final Logger log = LoggerFactory.getLogger(BillingWebhookController.class);

    private final List<BillingWebhookService> webhookServices;

    public BillingWebhookController(List<BillingWebhookService> webhookServices) {
        this.webhookServices = webhookServices;
    }

    @PostMapping("/stripe")
    public ResponseEntity<Map<String, String>> handleStripeWebhook(
            @RequestBody BillingWebhookEvent event) {

        log.info("Received Stripe webhook: {}", event.type());

        webhookServices.stream()
                .filter(service -> service.supportsEventType(event.type()))
                .findFirst()
                .ifPresent(service -> {
                    try {
                        service.processWebhook(event);
                    } catch (Exception e) {
                        log.error("Error processing webhook event {}: {}", event.type(), e.getMessage(), e);
                    }
                });

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "received"));
    }

    @PostMapping("/generic")
    public ResponseEntity<Map<String, String>> handleGenericWebhook(
            @RequestBody BillingWebhookEvent event) {

        log.info("Received generic webhook: {}", event.type());

        webhookServices.stream()
                .filter(service -> service.supportsEventType(event.type()))
                .findFirst()
                .ifPresent(service -> {
                    try {
                        service.processWebhook(event);
                    } catch (Exception e) {
                        log.error("Error processing webhook event {}: {}", event.type(), e.getMessage(), e);
                    }
                });

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "received"));
    }
}
