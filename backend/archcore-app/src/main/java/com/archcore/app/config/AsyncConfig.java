package com.archcore.app.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@EnableConfigurationProperties(AsyncProperties.class)
public class AsyncConfig {

    private final Map<String, ThreadPoolTaskExecutor> executors = new ConcurrentHashMap<>();

    public AsyncConfig(AsyncProperties properties) {
        properties.executors().forEach(config -> {
            ThreadPoolTaskExecutor executor = createExecutor(config);
            executors.put(config.name(), executor);
        });
    }

    @Bean(name = "auditLogExecutor")
    public Executor auditLogExecutor() {
        return executors.getOrDefault("auditLog", createDefaultExecutor());
    }

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        return executors.getOrDefault("default", createDefaultExecutor());
    }

    public Executor getExecutor(String name) {
        return executors.getOrDefault(name, createDefaultExecutor());
    }

    private ThreadPoolTaskExecutor createExecutor(AsyncProperties.ExecutorConfig config) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.corePoolSize());
        executor.setMaxPoolSize(config.maxPoolSize());
        executor.setQueueCapacity(config.queueCapacity());
        executor.setThreadNamePrefix(config.threadNamePrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(config.awaitTerminationSeconds());
        executor.initialize();
        return executor;
    }

    private ThreadPoolTaskExecutor createDefaultExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
