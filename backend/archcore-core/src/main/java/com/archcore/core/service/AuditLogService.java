package com.archcore.core.service;

import com.archcore.core.domain.AuditLog;
import com.archcore.core.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Async
    public void logActivity(String userId, String endpoint, String httpMethod,
                            String clientIpAddress, int httpStatusCode, String description,
                            long executionTimeMs, String userAgent) {
        try {
            AuditLog auditLog = new AuditLog(
                    userId,
                    endpoint,
                    httpMethod,
                    clientIpAddress,
                    httpStatusCode,
                    description,
                    executionTimeMs,
                    userAgent
            );
            auditLogRepository.save(auditLog);
            log.debug("Audit log created for user {} on {}", userId, endpoint);
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }

    @Async
    public void logActivity(String userId, String endpoint, String httpMethod,
                            String clientIpAddress, int httpStatusCode, String description,
                            long executionTimeMs, String userAgent, String additionalData) {
        try {
            AuditLog auditLog = new AuditLog(
                    userId,
                    endpoint,
                    httpMethod,
                    clientIpAddress,
                    httpStatusCode,
                    description,
                    executionTimeMs,
                    userAgent
            );
            auditLog.setAdditionalData(additionalData);
            auditLogRepository.save(auditLog);
            log.debug("Audit log created for user {} on {}", userId, endpoint);
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getUserActivityHistory(String userId, int page, int size) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(
                userId,
                org.springframework.data.domain.PageRequest.of(page, size)
        ).getContent();
    }

    @Transactional(readOnly = true)
    public long getUserActivityCount(String userId, Instant since) {
        return auditLogRepository.countByUserIdSince(userId, since);
    }
}
