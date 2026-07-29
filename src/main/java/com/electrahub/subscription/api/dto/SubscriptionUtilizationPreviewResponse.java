package com.electrahub.subscription.api.dto;

import com.electrahub.subscription.domain.BenefitDisplayMode;
import com.electrahub.subscription.domain.PricingModel;
import com.electrahub.subscription.domain.QuotaUnit;

import java.math.BigDecimal;
import java.util.UUID;

public record SubscriptionUtilizationPreviewResponse(
        UUID allocationId,
        UUID planId,
        String planCode,
        String planName,
        String currencyCode,
        UUID userId,
        UUID organizationId,
        UUID groupId,
        String sessionReference,
        BigDecimal chargingCost,
        BigDecimal sessionFee,
        BigDecimal idleFee,
        BigDecimal taxes,
        BigDecimal eligibleSubtotal,
        BigDecimal totalFeeDiscountAmount,
        BigDecimal sessionFeeDiscountAmount,
        BigDecimal totalDiscountAmount,
        BigDecimal finalChargeExcludingTax,
        BigDecimal finalChargeIncludingTax,
        int unitsConsumed,
        BigDecimal energyKwh,
        QuotaUnit quotaUnit,
        BigDecimal quotaConsumedValue,
        BigDecimal coveredEnergyKwh,
        BigDecimal uncoveredEnergyKwh,
        BigDecimal benefitAmount,
        BigDecimal regularAmount,
        BigDecimal grossAmount,
        BigDecimal netAmount,
        boolean quotaExhausted,
        PricingModel pricingModel,
        BenefitDisplayMode benefitDisplayMode,
        Integer remainingQuotaAfterUse,
        com.electrahub.subscription.domain.DiscountType energyDiscountType,
        BigDecimal energyDiscountValue,
        BigDecimal remainingQuotaValueAfterUse
) {
}
