package com.archcore.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "archcore.rate-limit")
public record RateLimitProperties(
    boolean enabled,
    KeyStrategy keyStrategy,
    List<FilterConfig> filters
) {
    public RateLimitProperties {
        filters = filters != null ? filters : new ArrayList<>();
        keyStrategy = keyStrategy != null ? keyStrategy : KeyStrategy.COMPOSITE;
    }

    public enum KeyStrategy {
        IP,
        USER_ID,
        API_KEY,
        COMPOSITE
    }

    public record FilterConfig(
        String url,
        int capacity,
        int refillTokens,
        int refillDurationSeconds
    ) {}
}
