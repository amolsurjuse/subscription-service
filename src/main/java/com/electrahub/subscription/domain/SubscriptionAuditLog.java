package com.electrahub.subscription.domain;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscription_audit_logs")
public class SubscriptionAuditLog {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionAuditLog.class);


    @Id
    private UUID id;

    @Column(name = "plan_id")
    private UUID planId;

    @Column(name = "allocation_id")
    private UUID allocationId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "group_id")
    private UUID groupId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private AuditAction action;

    @Column(nullable = false, length = 100)
    private String actor;

    @Column(nullable = false, length = 1024)
    private String detail;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    /**
     * Executes subscription audit log for `SubscriptionAuditLog`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     */
    protected SubscriptionAuditLog() {
        LOGGER.info("CODEx_ENTRY_LOG: Entering SubscriptionAuditLog#SubscriptionAuditLog");
        LOGGER.debug("CODEx_ENTRY_LOG: Entering SubscriptionAuditLog#SubscriptionAuditLog with debug context");
    }

    public SubscriptionAuditLog(UUID id,
                                UUID planId,
                                UUID allocationId,
                                UUID userId,
                                UUID organizationId,
                                UUID groupId,
                                AuditAction action,
                                String actor,
                                String detail,
                                OffsetDateTime createdAt) {
        this.id = id;
        this.planId = planId;
        this.allocationId = allocationId;
        this.userId = userId;
        this.organizationId = organizationId;
        this.groupId = groupId;
        this.action = action;
        this.actor = actor;
        this.detail = detail;
        this.createdAt = createdAt;
    }

    /**
     * Retrieves get id for `SubscriptionAuditLog`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getId.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Retrieves get plan id for `SubscriptionAuditLog`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getPlanId.
     */
    public UUID getPlanId() {
        return planId;
    }

    /**
     * Retrieves get allocation id for `SubscriptionAuditLog`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getAllocationId.
     */
    public UUID getAllocationId() {
        return allocationId;
    }

    /**
     * Retrieves get user id for `SubscriptionAuditLog`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getUserId.
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Retrieves get organization id for `SubscriptionAuditLog`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getOrganizationId.
     */
    public UUID getOrganizationId() {
        return organizationId;
    }

    /**
     * Retrieves get group id for `SubscriptionAuditLog`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getGroupId.
     */
    public UUID getGroupId() {
        return groupId;
    }

    /**
     * Retrieves get action for `SubscriptionAuditLog`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getAction.
     */
    public AuditAction getAction() {
        return action;
    }

    /**
     * Retrieves get actor for `SubscriptionAuditLog`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getActor.
     */
    public String getActor() {
        return actor;
    }

    /**
     * Retrieves get detail for `SubscriptionAuditLog`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getDetail.
     */
    public String getDetail() {
        return detail;
    }

    /**
     * Retrieves get created at for `SubscriptionAuditLog`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getCreatedAt.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
