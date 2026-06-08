package com.electrahub.subscription.api.dto;

import com.electrahub.subscription.domain.AllocationSource;
import com.electrahub.subscription.domain.QuotaUnit;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateSubscriptionGrantRequest(
        @NotNull UUID planId,
        @NotNull UUID userId,
        @NotNull @Positive BigDecimal quotaValue,
        QuotaUnit quotaUnit,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        @Size(max = 512) String grantReason,
        @Size(max = 120) String externalReference,
        @Size(max = 32) String vin,
        @Size(max = 64) String dealerCode,
        UUID enterpriseId,
        @Size(max = 100) String createdBy,
        AllocationSource source
) {
}
