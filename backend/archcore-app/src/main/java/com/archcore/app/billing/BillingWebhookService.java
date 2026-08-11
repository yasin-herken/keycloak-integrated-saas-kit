package com.archcore.app.billing;

public interface BillingWebhookService {

    void processWebhook(BillingWebhookEvent event);

    boolean supportsEventType(String eventType);
}
