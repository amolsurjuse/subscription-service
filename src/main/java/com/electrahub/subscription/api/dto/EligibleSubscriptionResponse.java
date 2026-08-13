package com.electrahub.subscription.api.dto;

import com.electrahub.subscription.domain.AutoApplyPolicy;
import com.electrahub.subscription.domain.ChargingScopeType;
import com.electrahub.subscription.domain.QuotaUnit;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record EligibleSubscriptionResponse(
        UUID allocationId,
        UUID planId,
        String planCode,
        String planName,
        boolean recommended,
        boolean selectedByDefault,
        String recommendationReason,
        BigDecimal remainingQuota,
        QuotaUnit quotaUnit,
        ChargingScopeType chargingScopeType,
        String chargingScopeReference,
        AutoApplyPolicy autoApplyPolicy,
        OffsetDateTime expiresAt
) {
}
