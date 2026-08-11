package com.archcore.core.domain;

import com.archcore.core.domain.enums.PlanTier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "plans")
public class Plan extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private PlanTier tier;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private int rateLimitPerMinute;

    @Column(nullable = false)
    private long maxProjects;

    @Column(nullable = false)
    private long maxMembers;

    @Column(nullable = false)
    private boolean active;

    protected Plan() {
        super();
    }

    public Plan(PlanTier tier, String name, String description, int rateLimitPerMinute,
                long maxProjects, long maxMembers) {
        super();
        this.tier = tier;
        this.name = name;
        this.description = description;
        this.rateLimitPerMinute = rateLimitPerMinute;
        this.maxProjects = maxProjects;
        this.maxMembers = maxMembers;
        this.active = true;
    }

    public PlanTier getTier() {
        return tier;
    }

    public void setTier(PlanTier tier) {
        this.tier = tier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    public void setRateLimitPerMinute(int rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
    }

    public long getMaxProjects() {
        return maxProjects;
    }

    public void setMaxProjects(long maxProjects) {
        this.maxProjects = maxProjects;
    }

    public long getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(long maxMembers) {
        this.maxMembers = maxMembers;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
