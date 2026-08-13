package com.electrahub.subscription.api.dto;

import com.electrahub.subscription.domain.AllocationSource;
import com.electrahub.subscription.domain.QuotaUnit;
import com.electrahub.subscription.domain.AutoApplyPolicy;
import com.electrahub.subscription.domain.BeneficiaryType;
import com.electrahub.subscription.domain.ChargingScopeType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateSubscriptionGrantRequest(
        @NotNull UUID planId,
        @NotNull UUID userId,
        @NotNull @Positive BigDecimal quotaValue,
        QuotaUnit quotaUnit,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        @Size(max = 512) String grantReason,
        @Size(max = 120) String externalReference,
        @Size(max = 32) String vin,
        @Size(max = 64) String dealerCode,
        UUID enterpriseId,
        @Size(max = 100) String createdBy,
        AllocationSource source,
        BeneficiaryType beneficiaryType,
        @Size(max = 128) String beneficiaryReference,
        ChargingScopeType chargingScopeType,
        @Size(max = 128) String chargingScopeReference,
        AutoApplyPolicy autoApplyPolicy
) {
    public CreateSubscriptionGrantRequest(UUID planId,
                                          UUID userId,
                                          BigDecimal quotaValue,
                                          QuotaUnit quotaUnit,
                                          OffsetDateTime startsAt,
                                          OffsetDateTime endsAt,
                                          String grantReason,
                                          String externalReference,
                                          String vin,
                                          String dealerCode,
                                          UUID enterpriseId,
                                          String createdBy,
                                          AllocationSource source) {
        this(planId, userId, quotaValue, quotaUnit, startsAt, endsAt, grantReason, externalReference,
                vin, dealerCode, enterpriseId, createdBy, source,
                null, null, null, null, null);
    }
}
