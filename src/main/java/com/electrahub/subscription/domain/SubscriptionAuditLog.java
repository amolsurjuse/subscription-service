package com.electrahub.subscription.domain;

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

    protected SubscriptionAuditLog() {
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

    public UUID getId() {
        return id;
    }

    public UUID getPlanId() {
        return planId;
    }

    public UUID getAllocationId() {
        return allocationId;
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

    public AuditAction getAction() {
        return action;
    }

    public String getActor() {
        return actor;
    }

    public String getDetail() {
        return detail;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
