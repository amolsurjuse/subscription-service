package com.electrahub.subscription.api.dto;

import com.electrahub.subscription.domain.AllocationStatus;
import com.electrahub.subscription.domain.AllocationType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SubscriptionAllocationResponse(
        UUID id,
        UUID planId,
        String planCode,
        String planName,
        String currencyCode,
        AllocationType allocationType,
        UUID userId,
        UUID organizationId,
        UUID groupId,
        Integer quotaLimit,
        Integer effectiveQuotaLimit,
        int consumedUnits,
        Integer remainingQuota,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        AllocationStatus status,
        String createdBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
