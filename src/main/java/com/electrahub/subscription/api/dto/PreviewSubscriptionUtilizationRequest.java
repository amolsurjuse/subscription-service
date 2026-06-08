package com.electrahub.subscription.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record PreviewSubscriptionUtilizationRequest(
        UUID allocationId,
        @NotNull UUID userId,
        UUID organizationId,
        UUID groupId,
        @Size(max = 120) String sessionReference,
        @NotNull @PositiveOrZero BigDecimal chargingCost,
        @NotNull @PositiveOrZero BigDecimal sessionFee,
        @NotNull @PositiveOrZero BigDecimal idleFee,
        @NotNull @PositiveOrZero BigDecimal taxes,
        @Positive Integer unitsConsumed,
        @Positive BigDecimal energyKwh,
        @Positive BigDecimal quotaConsumedValue
) {
    public PreviewSubscriptionUtilizationRequest(UUID allocationId,
                                                 UUID userId,
                                                 UUID organizationId,
                                                 UUID groupId,
                                                 String sessionReference,
                                                 BigDecimal chargingCost,
                                                 BigDecimal sessionFee,
                                                 BigDecimal idleFee,
                                                 BigDecimal taxes,
                                                 Integer unitsConsumed) {
        this(
                allocationId,
                userId,
                organizationId,
                groupId,
                sessionReference,
                chargingCost,
                sessionFee,
                idleFee,
                taxes,
                unitsConsumed,
                null,
                null
        );
    }
}
