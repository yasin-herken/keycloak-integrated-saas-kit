package com.archcore.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, length = 500)
    private String endpoint;

    @Column(nullable = false, length = 10)
    private String httpMethod;

    @Column(nullable = false)
    private String clientIpAddress;

    @Column(nullable = false)
    private int httpStatusCode;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private long executionTimeMs;

    @Column(length = 500)
    private String userAgent;

    @Column(length = 1000)
    private String additionalData;

    protected AuditLog() {
        super();
    }

    public AuditLog(String userId, String endpoint, String httpMethod, String clientIpAddress,
                    int httpStatusCode, String description, long executionTimeMs, String userAgent) {
        super();
        this.userId = userId;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.clientIpAddress = clientIpAddress;
        this.httpStatusCode = httpStatusCode;
        this.description = description;
        this.executionTimeMs = executionTimeMs;
        this.userAgent = userAgent;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getClientIpAddress() {
        return clientIpAddress;
    }

    public void setClientIpAddress(String clientIpAddress) {
        this.clientIpAddress = clientIpAddress;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }
}
