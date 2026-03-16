package com.electrahub.subscription.api.dto;

import com.electrahub.subscription.domain.DiscountType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SubscriptionPlanResponse(
        UUID id,
        String code,
        String name,
        String description,
        String currencyCode,
        DiscountType totalFeeDiscountType,
        BigDecimal totalFeeDiscountValue,
        DiscountType sessionFeeDiscountType,
        BigDecimal sessionFeeDiscountValue,
        Integer defaultQuotaLimit,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
