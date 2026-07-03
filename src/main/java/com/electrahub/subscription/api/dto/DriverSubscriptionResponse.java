package com.electrahub.subscription.api.dto;

import com.electrahub.subscription.domain.AllocationStatus;
import com.electrahub.subscription.domain.BenefitDisplayMode;
import com.electrahub.subscription.domain.PricingModel;
import com.electrahub.subscription.domain.QuotaUnit;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DriverSubscriptionResponse(
        UUID allocationId,
        UUID planId,
        String planCode,
        String planName,
        String source,
        String sourceLabel,
        PricingModel pricingModel,
        BenefitDisplayMode benefitDisplayMode,
        QuotaUnit quotaUnit,
        BigDecimal quotaLimitValue,
        BigDecimal consumedValue,
        BigDecimal remainingValue,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        AllocationStatus status
) {
}
