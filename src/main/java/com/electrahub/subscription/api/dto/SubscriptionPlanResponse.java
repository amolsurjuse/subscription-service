package com.electrahub.subscription.api.dto;

import com.electrahub.subscription.domain.DiscountType;
import com.electrahub.subscription.domain.BenefitDisplayMode;
import com.electrahub.subscription.domain.PlanCategory;
import com.electrahub.subscription.domain.PlanVisibility;
import com.electrahub.subscription.domain.PricingModel;
import com.electrahub.subscription.domain.QuotaUnit;

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
        PlanVisibility visibility,
        PlanCategory planCategory,
        PricingModel pricingModel,
        BenefitDisplayMode benefitDisplayMode,
        QuotaUnit quotaUnit,
        BigDecimal defaultQuotaValue,
        BigDecimal subscriptionPriceAmount,
        Integer validityDays,
        UUID enterpriseId,
        String countryCode,
        Integer publicSortOrder,
        boolean allowStacking,
        String createdBy,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
