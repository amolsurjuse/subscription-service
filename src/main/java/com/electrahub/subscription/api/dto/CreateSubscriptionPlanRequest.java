package com.electrahub.subscription.api.dto;

import com.electrahub.subscription.domain.DiscountType;
import com.electrahub.subscription.domain.BenefitDisplayMode;
import com.electrahub.subscription.domain.PlanCategory;
import com.electrahub.subscription.domain.PlanVisibility;
import com.electrahub.subscription.domain.PricingModel;
import com.electrahub.subscription.domain.QuotaUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateSubscriptionPlanRequest(
        @NotBlank @Size(max = 64) String code,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 1024) String description,
        @NotBlank @Size(min = 3, max = 3) String currencyCode,
        @NotNull DiscountType totalFeeDiscountType,
        @NotNull @PositiveOrZero BigDecimal totalFeeDiscountValue,
        @NotNull DiscountType sessionFeeDiscountType,
        @NotNull @PositiveOrZero BigDecimal sessionFeeDiscountValue,
        @Positive Integer defaultQuotaLimit,
        PlanVisibility visibility,
        PlanCategory planCategory,
        PricingModel pricingModel,
        BenefitDisplayMode benefitDisplayMode,
        QuotaUnit quotaUnit,
        @Positive BigDecimal defaultQuotaValue,
        @PositiveOrZero BigDecimal subscriptionPriceAmount,
        @Positive Integer validityDays,
        UUID enterpriseId,
        @Size(min = 2, max = 2) String countryCode,
        Integer publicSortOrder,
        Boolean allowStacking,
        @Size(max = 100) String createdBy,
        Boolean active
) {
}
