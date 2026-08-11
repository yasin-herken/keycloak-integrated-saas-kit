package com.archcore.app.billing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BillingWebhookEvent(
    String id,
    String type,
    @JsonProperty("created") long createdTimestamp,
    Map<String, Object> data
) {
    public Instant getCreatedAt() {
        return Instant.ofEpochSecond(createdTimestamp);
    }

    public String getExternalSubscriptionId() {
        if (data != null && data.containsKey("object")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> object = (Map<String, Object>) data.get("object");
            if (object != null && object.containsKey("id")) {
                return object.get("id").toString();
            }
        }
        return null;
    }
}
