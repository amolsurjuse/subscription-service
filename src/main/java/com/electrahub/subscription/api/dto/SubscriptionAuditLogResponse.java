package com.electrahub.subscription.api.dto;

import com.electrahub.subscription.domain.AuditAction;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SubscriptionAuditLogResponse(
        UUID id,
        UUID planId,
        UUID allocationId,
        UUID userId,
        UUID organizationId,
        UUID groupId,
        AuditAction action,
        String actor,
        String detail,
        OffsetDateTime createdAt
) {
}
