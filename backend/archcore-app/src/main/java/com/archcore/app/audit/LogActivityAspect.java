package com.archcore.app.audit;

import com.archcore.core.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class LogActivityAspect {

    private static final Logger log = LoggerFactory.getLogger(LogActivityAspect.class);

    private final AuditLogService auditLogService;

    public LogActivityAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Around("@annotation(logActivity)")
    public Object logActivity(ProceedingJoinPoint joinPoint, LogActivity logActivity) throws Throwable {
        long startTime = System.currentTimeMillis();
        HttpServletRequest request = getCurrentRequest();
        String userId = getUserId();
        int httpStatusCode = 200;
        Object result = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            httpStatusCode = 500;
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;

            String description = logActivity.description().isEmpty()
                    ? joinPoint.getSignature().toShortString()
                    : logActivity.description();

            String clientIp = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");

            auditLogService.logActivity(
                    userId,
                    request.getRequestURI(),
                    request.getMethod(),
                    clientIp,
                    httpStatusCode,
                    description,
                    executionTime,
                    userAgent
            );
        }
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
