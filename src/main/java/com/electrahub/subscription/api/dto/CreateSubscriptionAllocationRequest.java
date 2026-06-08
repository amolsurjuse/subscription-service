package com.electrahub.subscription.api.dto;

import com.electrahub.subscription.domain.AllocationStatus;
import com.electrahub.subscription.domain.AllocationSource;
import com.electrahub.subscription.domain.AllocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateSubscriptionAllocationRequest(
        @NotNull UUID planId,
        @NotNull AllocationType allocationType,
        UUID userId,
        UUID organizationId,
        UUID groupId,
        @Positive Integer quotaLimit,
        @Positive BigDecimal quotaLimitValue,
        @NotNull OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        AllocationStatus status,
        @NotBlank String createdBy,
        AllocationSource source,
        String sourceLabel,
        String grantReason,
        String externalReference,
        String vin,
        UUID enterpriseId
) {
}
