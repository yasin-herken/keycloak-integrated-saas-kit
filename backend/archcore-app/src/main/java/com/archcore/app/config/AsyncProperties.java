package com.archcore.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "archcore.async")
public record AsyncProperties(
    boolean enabled,
    List<ExecutorConfig> executors
) {
    public AsyncProperties {
        executors = executors != null ? executors : new ArrayList<>();
    }

    public record ExecutorConfig(
        String name,
        int corePoolSize,
        int maxPoolSize,
        int queueCapacity,
        String threadNamePrefix,
        int awaitTerminationSeconds
    ) {}
}
