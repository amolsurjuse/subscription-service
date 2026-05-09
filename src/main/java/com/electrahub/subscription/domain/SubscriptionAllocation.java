package com.electrahub.subscription.domain;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionAllocation.class);


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

    /**
     * Executes subscription allocation for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     */
    protected SubscriptionAllocation() {
        LOGGER.info(" Entering SubscriptionAllocation#SubscriptionAllocation");
        LOGGER.debug(" Entering SubscriptionAllocation#SubscriptionAllocation with debug context");
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

    /**
     * Executes pre update for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     */
    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Retrieves get id for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getId.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Retrieves get plan for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getPlan.
     */
    public SubscriptionPlan getPlan() {
        return plan;
    }

    /**
     * Retrieves get allocation type for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getAllocationType.
     */
    public AllocationType getAllocationType() {
        return allocationType;
    }

    /**
     * Retrieves get user id for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getUserId.
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Retrieves get organization id for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getOrganizationId.
     */
    public UUID getOrganizationId() {
        return organizationId;
    }

    /**
     * Retrieves get group id for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getGroupId.
     */
    public UUID getGroupId() {
        return groupId;
    }

    /**
     * Retrieves get quota limit for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getQuotaLimit.
     */
    public Integer getQuotaLimit() {
        return quotaLimit;
    }

    /**
     * Retrieves get consumed units for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getConsumedUnits.
     */
    public int getConsumedUnits() {
        return consumedUnits;
    }

    /**
     * Retrieves get starts at for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getStartsAt.
     */
    public OffsetDateTime getStartsAt() {
        return startsAt;
    }

    /**
     * Retrieves get ends at for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getEndsAt.
     */
    public OffsetDateTime getEndsAt() {
        return endsAt;
    }

    /**
     * Retrieves get status for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getStatus.
     */
    public AllocationStatus getStatus() {
        return status;
    }

    /**
     * Retrieves get created by for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getCreatedBy.
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Retrieves get created at for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getCreatedAt.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Retrieves get updated at for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getUpdatedAt.
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Retrieves get effective quota limit for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getEffectiveQuotaLimit.
     */
    public Integer getEffectiveQuotaLimit() {
        return quotaLimit != null ? quotaLimit : plan.getDefaultQuotaLimit();
    }

    /**
     * Retrieves get remaining quota for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getRemainingQuota.
     */
    public Integer getRemainingQuota() {
        Integer effectiveQuotaLimit = getEffectiveQuotaLimit();
        if (effectiveQuotaLimit == null) {
            return null;
        }
        return Math.max(0, effectiveQuotaLimit - consumedUnits);
    }

    /**
     * Executes is active at for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @param time input consumed by isActiveAt.
     * @return result produced by isActiveAt.
     */
    public boolean isActiveAt(OffsetDateTime time) {
        boolean withinWindow = !time.isBefore(startsAt) && (endsAt == null || !time.isAfter(endsAt));
        return status == AllocationStatus.ACTIVE && withinWindow && plan.isActive();
    }

    /**
     * Executes increment consumed units for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @param units input consumed by incrementConsumedUnits.
     */
    public void incrementConsumedUnits(int units) {
        this.consumedUnits += units;
    }

    /**
     * Updates set status for `SubscriptionAllocation`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @param status input consumed by setStatus.
     */
    public void setStatus(AllocationStatus status) {
        this.status = status;
    }
}
