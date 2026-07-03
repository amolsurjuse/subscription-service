package com.electrahub.subscription.api.dto;

import com.electrahub.subscription.domain.BenefitDisplayMode;
import com.electrahub.subscription.domain.DiscountType;
import com.electrahub.subscription.domain.PlanCategory;
import com.electrahub.subscription.domain.PricingModel;
import com.electrahub.subscription.domain.QuotaUnit;

import java.math.BigDecimal;
import java.util.UUID;

public record DriverSubscriptionPlanResponse(
        UUID planId,
        String code,
        String name,
        String description,
        String currencyCode,
        PlanCategory planCategory,
        PricingModel pricingModel,
        BenefitDisplayMode benefitDisplayMode,
        QuotaUnit quotaUnit,
        BigDecimal quotaLimitValue,
        Integer validityDays,
        DiscountType totalFeeDiscountType,
        BigDecimal totalFeeDiscountValue,
        DiscountType sessionFeeDiscountType,
        BigDecimal sessionFeeDiscountValue,
        boolean active
) {
}
