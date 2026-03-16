package com.electrahub.subscription.api.dto;

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
        Integer remainingQuotaAfterUse
) {
}
