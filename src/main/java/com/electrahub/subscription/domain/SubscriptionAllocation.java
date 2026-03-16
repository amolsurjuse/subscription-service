package com.electrahub.subscription.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscription_allocations")
public class SubscriptionAllocation {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "allocation_type", nullable = false, length = 32)
    private AllocationType allocationType;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "quota_limit")
    private Integer quotaLimit;

    @Column(name = "consumed_units", nullable = false)
    private int consumedUnits;

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt;

    @Column(name = "ends_at")
    private OffsetDateTime endsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AllocationStatus status;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected SubscriptionAllocation() {
    }

    public SubscriptionAllocation(UUID id,
                                  SubscriptionPlan plan,
                                  AllocationType allocationType,
                                  UUID userId,
                                  UUID organizationId,
                                  UUID groupId,
                                  Integer quotaLimit,
                                  OffsetDateTime startsAt,
                                  OffsetDateTime endsAt,
                                  AllocationStatus status,
                                  String createdBy,
                                  OffsetDateTime now) {
        this.id = id;
        this.plan = plan;
        this.allocationType = allocationType;
        this.userId = userId;
        this.organizationId = organizationId;
        this.groupId = groupId;
        this.quotaLimit = quotaLimit;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public AllocationType getAllocationType() {
        return allocationType;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public Integer getQuotaLimit() {
        return quotaLimit;
    }

    public int getConsumedUnits() {
        return consumedUnits;
    }

    public OffsetDateTime getStartsAt() {
        return startsAt;
    }

    public OffsetDateTime getEndsAt() {
        return endsAt;
    }

    public AllocationStatus getStatus() {
        return status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Integer getEffectiveQuotaLimit() {
        return quotaLimit != null ? quotaLimit : plan.getDefaultQuotaLimit();
    }

    public Integer getRemainingQuota() {
        Integer effectiveQuotaLimit = getEffectiveQuotaLimit();
        if (effectiveQuotaLimit == null) {
            return null;
        }
        return Math.max(0, effectiveQuotaLimit - consumedUnits);
    }

    public boolean isActiveAt(OffsetDateTime time) {
        boolean withinWindow = !time.isBefore(startsAt) && (endsAt == null || !time.isAfter(endsAt));
        return status == AllocationStatus.ACTIVE && withinWindow && plan.isActive();
    }

    public void incrementConsumedUnits(int units) {
        this.consumedUnits += units;
    }

    public void setStatus(AllocationStatus status) {
        this.status = status;
    }
}
