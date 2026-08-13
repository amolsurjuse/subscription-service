package com.electrahub.subscription.api.dto;

import com.electrahub.subscription.domain.AllocationStatus;
import com.electrahub.subscription.domain.AllocationSource;
import com.electrahub.subscription.domain.AllocationType;
import com.electrahub.subscription.domain.AutoApplyPolicy;
import com.electrahub.subscription.domain.BeneficiaryType;
import com.electrahub.subscription.domain.ChargingScopeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateSubscriptionAllocationRequest(
        @NotNull UUID planId,
        @NotNull AllocationType allocationType,
        UUID userId,
        UUID organizationId,
        UUID groupId,
        @Positive Integer quotaLimit,
        @Positive BigDecimal quotaLimitValue,
        @NotNull OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        AllocationStatus status,
        @NotBlank String createdBy,
        AllocationSource source,
        String sourceLabel,
        String grantReason,
        String externalReference,
        String vin,
        UUID enterpriseId,
        BeneficiaryType beneficiaryType,
        String beneficiaryReference,
        ChargingScopeType chargingScopeType,
        String chargingScopeReference,
        AutoApplyPolicy autoApplyPolicy
) {
    public CreateSubscriptionAllocationRequest(UUID planId,
                                               AllocationType allocationType,
                                               UUID userId,
                                               UUID organizationId,
                                               UUID groupId,
                                               Integer quotaLimit,
                                               BigDecimal quotaLimitValue,
                                               OffsetDateTime startsAt,
                                               OffsetDateTime endsAt,
                                               AllocationStatus status,
                                               String createdBy,
                                               AllocationSource source,
                                               String sourceLabel,
                                               String grantReason,
                                               String externalReference,
                                               String vin,
                                               UUID enterpriseId) {
        this(planId, allocationType, userId, organizationId, groupId, quotaLimit, quotaLimitValue,
                startsAt, endsAt, status, createdBy, source, sourceLabel, grantReason,
                externalReference, vin, enterpriseId, null, null, null, null, null);
    }
}
