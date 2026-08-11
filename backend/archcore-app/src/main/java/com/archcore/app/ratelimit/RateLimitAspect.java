package com.archcore.app.ratelimit;

import com.archcore.core.service.BillingService;
import com.archcore.core.domain.Subscription;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    private final ProxyManager<String> proxyManager;
    private final BillingService billingService;
    private final ConcurrentHashMap<String, BucketConfiguration> configCache = new ConcurrentHashMap<>();

    public RateLimitAspect(ProxyManager<String> proxyManager, BillingService billingService) {
        this.proxyManager = proxyManager;
        this.billingService = billingService;
    }

    @Around("@annotation(rateLimit)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = getCurrentRequest();
        String rateLimitKey = buildRateLimitKey(request, joinPoint, rateLimit);

        int effectiveRequests = getEffectiveRequestLimit(rateLimit);
        int effectivePeriod = rateLimit.periodSeconds();

        BucketConfiguration config = configCache.computeIfAbsent(
                rateLimitKey,
                k -> BucketConfiguration.builder()
                        .addLimit(Bandwidth.simple(effectiveRequests, Duration.ofSeconds(effectivePeriod)))
                        .build()
        );

        ConsumptionProbe probe = proxyManager.builder().build(rateLimitKey, config).tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            return joinPoint.proceed();
        } else {
            long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            log.warn("Rate limit exceeded for key: {} on method: {}",
                    rateLimitKey, joinPoint.getSignature().toShortString());
            throw new RateLimitExceededException(retryAfterSeconds);
        }
    }

    private int getEffectiveRequestLimit(RateLimit rateLimit) {
        int baseLimit = rateLimit.requests();

        if (rateLimit.scope() == RateLimit.RateLimitScope.USER) {
            Optional<Integer> planLimit = getPlanBasedLimit();
            if (planLimit.isPresent()) {
                return Math.max(baseLimit, planLimit.get());
            }
        }

        return baseLimit;
    }

    private Optional<Integer> getPlanBasedLimit() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String userId = jwt.getSubject();
            return billingService.getActiveSubscription(userId)
                    .map(subscription -> subscription.getPlan().getRateLimitPerMinute());
        }
        return Optional.empty();
    }

    private String buildRateLimitKey(HttpServletRequest request, ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        String prefix = rateLimit.key().isEmpty()
                ? joinPoint.getSignature().toShortString()
                : rateLimit.key();

        return switch (rateLimit.scope()) {
            case IP -> "ip:" + getClientIp(request) + ":" + prefix;
            case USER -> "user:" + getUserId() + ":" + prefix;
            case ENDPOINT -> "endpoint:" + request.getRequestURI();
        };
    }

    private String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return "anonymous";
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
        return request.getRemoteAddr();
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("No current HTTP request available");
        }
        return attributes.getRequest();
    }
}
