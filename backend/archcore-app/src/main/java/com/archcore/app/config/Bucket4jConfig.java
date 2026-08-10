package com.archcore.app.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.caffeine.Bucket4jCaffeine;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class Bucket4jConfig {

    @Bean
    public ProxyManager<String> bucketProxyManager() {
        Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
                .maximumSize(10_000);

        @SuppressWarnings("unchecked")
        ProxyManager<String> proxyManager = (ProxyManager<String>) (ProxyManager<?>) Bucket4jCaffeine.builderFor((Caffeine<?, ?>) caffeineBuilder)
                .build();
        return proxyManager;
    }
}
