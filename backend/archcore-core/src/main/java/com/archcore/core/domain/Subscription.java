package com.archcore.core.domain;

import com.archcore.core.domain.enums.SubscriptionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "subscriptions")
public class Subscription extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SubscriptionStatus status;

    @Column(nullable = false)
    private Instant startDate;

    @Column
    private Instant endDate;

    @Column(nullable = false)
    private Instant billingCycleStart;

    @Column(nullable = false)
    private Instant billingCycleEnd;

    @Column(length = 255)
    private String externalSubscriptionId;

    @Column(nullable = false)
    private boolean autoRenew;

    protected Subscription() {
        super();
    }

    public Subscription(String userId, Plan plan, SubscriptionStatus status,
                        Instant startDate, Instant billingCycleStart, Instant billingCycleEnd) {
        super();
        this.userId = userId;
        this.plan = plan;
        this.status = status;
        this.startDate = startDate;
        this.billingCycleStart = billingCycleStart;
        this.billingCycleEnd = billingCycleEnd;
        this.autoRenew = true;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public Instant getBillingCycleStart() {
        return billingCycleStart;
    }

    public void setBillingCycleStart(Instant billingCycleStart) {
        this.billingCycleStart = billingCycleStart;
    }

    public Instant getBillingCycleEnd() {
        return billingCycleEnd;
    }

    public void setBillingCycleEnd(Instant billingCycleEnd) {
        this.billingCycleEnd = billingCycleEnd;
    }

    public String getExternalSubscriptionId() {
        return externalSubscriptionId;
    }

    public void setExternalSubscriptionId(String externalSubscriptionId) {
        this.externalSubscriptionId = externalSubscriptionId;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public boolean isActive() {
        return SubscriptionStatus.ACTIVE.equals(this.status);
    }

    public boolean isExpired() {
        return this.billingCycleEnd != null && this.billingCycleEnd.isBefore(Instant.now());
    }
}
