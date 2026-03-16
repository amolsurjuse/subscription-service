package com.electrahub.subscription.api.dto;

import com.electrahub.subscription.domain.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateSubscriptionPlanRequest(
        @NotBlank @Size(max = 64) String code,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 1024) String description,
        @NotBlank @Size(min = 3, max = 3) String currencyCode,
        @NotNull DiscountType totalFeeDiscountType,
        @NotNull @PositiveOrZero BigDecimal totalFeeDiscountValue,
        @NotNull DiscountType sessionFeeDiscountType,
        @NotNull @PositiveOrZero BigDecimal sessionFeeDiscountValue,
        @Positive Integer defaultQuotaLimit
) {
}
