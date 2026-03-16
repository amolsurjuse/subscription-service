package com.electrahub.subscription.api.dto;

import com.electrahub.subscription.domain.AllocationStatus;
import com.electrahub.subscription.domain.AllocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateSubscriptionAllocationRequest(
        @NotNull UUID planId,
        @NotNull AllocationType allocationType,
        UUID userId,
        UUID organizationId,
        UUID groupId,
        @Positive Integer quotaLimit,
        @NotNull OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        AllocationStatus status,
        @NotBlank String createdBy
) {
}
