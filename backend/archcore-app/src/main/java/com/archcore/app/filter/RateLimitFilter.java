package com.archcore.app.filter;

import com.archcore.app.config.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final ProxyManager<String> proxyManager;
    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final Map<String, BucketConfiguration> configCache = new ConcurrentHashMap<>();

    public RateLimitFilter(ProxyManager<String> proxyManager,
                           RateLimitProperties properties,
                           ObjectMapper objectMapper) {
        this.proxyManager = proxyManager;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!properties.enabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestUri = request.getRequestURI();

        for (RateLimitProperties.FilterConfig filterConfig : properties.filters()) {
            if (matchesUrl(requestUri, filterConfig.url())) {
                String rateLimitKey = buildRateLimitKey(request, requestUri);
                BucketConfiguration config = configCache.computeIfAbsent(
                        filterConfig.url(),
                        k -> BucketConfiguration.builder()
                                .addLimit(Bandwidth.simple(filterConfig.capacity(),
                                        Duration.ofSeconds(filterConfig.refillDurationSeconds())))
                                .build()
                );

                ConsumptionProbe probe = proxyManager.builder().build(rateLimitKey, config).tryConsumeAndReturnRemaining(1);
                if (probe.isConsumed()) {
                    response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
                    response.addHeader("X-Rate-Limit-Key-Strategy", properties.keyStrategy().name());
                    filterChain.doFilter(request, response);
                    return;
                } else {
                    long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
                    response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(retryAfterSeconds));
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    objectMapper.writeValue(response.getWriter(), Map.of(
                            "status", 429,
                            "error", "Too Many Requests",
                            "message", "Rate limit exceeded. Try again later.",
                            "path", requestUri,
                            "strategy", properties.keyStrategy().name(),
                            "retryAfterSeconds", retryAfterSeconds,
                            "timestamp", java.time.Instant.now().toString()
                    ));
                    log.warn("Rate limit exceeded for key: {} on path: {}", rateLimitKey, requestUri);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Rate limit key'i oluşturur.
     * 
     * Kurumsal ortamda IP tabanlı rate limiting çalışmaz çünkü:
     * - Tüm kullanıcılar aynı NAT/proxy IP'sinden görünür
     * - Load balancer IP'yi maskeler
     * - VPN kullanımında tüm trafik tek IP'den gelir
     * 
     * Composite strateji ile:
     * - Authenticated: user:{userId}:{endpoint} 
     * - Anonymous: ip:{clientIp}:{endpoint}
     * - API Key: apikey:{key}:{endpoint}
     */
    private String buildRateLimitKey(HttpServletRequest request, String requestUri) {
        return switch (properties.keyStrategy()) {
            case USER_ID -> {
                String userId = extractUserIdFromJwt();
                if (userId != null) {
                    yield "user:" + userId + ":" + requestUri;
                }
                yield "ip:" + getClientIp(request) + ":" + requestUri;
            }
            case API_KEY -> {
                String apiKey = extractApiKey(request);
                if (apiKey != null) {
                    yield "apikey:" + apiKey + ":" + requestUri;
                }
                yield "ip:" + getClientIp(request) + ":" + requestUri;
            }
            case IP -> "ip:" + getClientIp(request) + ":" + requestUri;
            case COMPOSITE -> {
                // Öncelik: API Key > User ID > IP
                String apiKey = extractApiKey(request);
                if (apiKey != null) {
                    yield "apikey:" + apiKey + ":" + requestUri;
                }
                String userId = extractUserIdFromJwt();
                if (userId != null) {
                    yield "user:" + userId + ":" + requestUri;
                }
                yield "ip:" + getClientIp(request) + ":" + requestUri;
            }
        };
    }

    /**
     * JWT token'dan user ID Extract eder.
     * Keycloak JWT'lerinde "sub" claim'i user ID'dir.
     */
    private String extractUserIdFromJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return null;
    }

    /**
     * API Key'i header'dan extract eder.
     * X-API-Key veya Authorization header'ından API key alınabilir.
     */
    private String extractApiKey(HttpServletRequest request) {
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey;
        }
        return null;
    }

    private boolean matchesUrl(String requestUri, String pattern) {
        return requestUri.matches(pattern.replace(".*", ".*"));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp;
        }
        String cfConnectingIp = request.getHeader("CF-Connecting-IP");
        if (cfConnectingIp != null && !cfConnectingIp.isBlank()) {
            return cfConnectingIp;
        }
        return request.getRemoteAddr();
    }
}
