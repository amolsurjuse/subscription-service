package com.electrahub.subscription.service;

import com.electrahub.subscription.api.dto.PreviewSubscriptionUtilizationRequest;
import com.electrahub.subscription.api.dto.RecordSubscriptionUtilizationRequest;
import com.electrahub.subscription.api.dto.SubscriptionUtilizationPreviewResponse;
import com.electrahub.subscription.api.dto.SubscriptionUtilizationResponse;
import com.electrahub.subscription.api.error.NotFoundException;
import com.electrahub.subscription.domain.AllocationType;
import com.electrahub.subscription.domain.AuditAction;
import com.electrahub.subscription.domain.DiscountType;
import com.electrahub.subscription.domain.SubscriptionAllocation;
import com.electrahub.subscription.domain.SubscriptionPlan;
import com.electrahub.subscription.domain.SubscriptionUtilization;
import com.electrahub.subscription.repository.SubscriptionAllocationRepository;
import com.electrahub.subscription.repository.SubscriptionUtilizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionPricingService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final SubscriptionAllocationRepository subscriptionAllocationRepository;
    private final SubscriptionUtilizationRepository subscriptionUtilizationRepository;
    private final SubscriptionAllocationService subscriptionAllocationService;
    private final SubscriptionAuditService subscriptionAuditService;

    public SubscriptionPricingService(SubscriptionAllocationRepository subscriptionAllocationRepository,
                                      SubscriptionUtilizationRepository subscriptionUtilizationRepository,
                                      SubscriptionAllocationService subscriptionAllocationService,
                                      SubscriptionAuditService subscriptionAuditService) {
        this.subscriptionAllocationRepository = subscriptionAllocationRepository;
        this.subscriptionUtilizationRepository = subscriptionUtilizationRepository;
        this.subscriptionAllocationService = subscriptionAllocationService;
        this.subscriptionAuditService = subscriptionAuditService;
    }

    @Transactional(readOnly = true)
    public SubscriptionUtilizationPreviewResponse preview(PreviewSubscriptionUtilizationRequest request) {
        int units = normalizeUnits(request.unitsConsumed());
        SubscriptionAllocation allocation = resolveAllocation(
                request.allocationId(),
                request.userId(),
                request.organizationId(),
                request.groupId(),
                units
        );
        PricingResult pricingResult = calculatePricing(
                allocation.getPlan(),
                request.chargingCost(),
                request.sessionFee(),
                request.idleFee(),
                request.taxes()
        );

        return new SubscriptionUtilizationPreviewResponse(
                allocation.getId(),
                allocation.getPlan().getId(),
                allocation.getPlan().getCode(),
                allocation.getPlan().getName(),
                allocation.getPlan().getCurrencyCode(),
                request.userId(),
                request.organizationId(),
                request.groupId(),
                normalizeOptionalText(request.sessionReference()),
                pricingResult.chargingCost(),
                pricingResult.sessionFee(),
                pricingResult.idleFee(),
                pricingResult.taxes(),
                pricingResult.eligibleSubtotal(),
                pricingResult.totalFeeDiscountAmount(),
                pricingResult.sessionFeeDiscountAmount(),
                pricingResult.totalDiscountAmount(),
                pricingResult.finalChargeExcludingTax(),
                pricingResult.finalChargeIncludingTax(),
                units,
                remainingQuotaAfterUse(allocation, units)
        );
    }

    @Transactional
    public SubscriptionUtilizationResponse record(RecordSubscriptionUtilizationRequest request) {
        int units = normalizeUnits(request.unitsConsumed());
        SubscriptionAllocation allocation = resolveAllocation(
                request.allocationId(),
                request.userId(),
                request.organizationId(),
                request.groupId(),
                units
        );
        PricingResult pricingResult = calculatePricing(
                allocation.getPlan(),
                request.chargingCost(),
                request.sessionFee(),
                request.idleFee(),
                request.taxes()
        );

        allocation.incrementConsumedUnits(units);
        Integer remainingQuota = allocation.getRemainingQuota();

        SubscriptionUtilization utilization = new SubscriptionUtilization(
                UUID.randomUUID(),
                allocation,
                allocation.getPlan(),
                request.userId(),
                request.organizationId(),
                request.groupId(),
                normalizeOptionalText(request.sessionReference()),
                pricingResult.chargingCost(),
                pricingResult.sessionFee(),
                pricingResult.idleFee(),
                pricingResult.taxes(),
                pricingResult.eligibleSubtotal(),
                pricingResult.totalFeeDiscountAmount(),
                pricingResult.sessionFeeDiscountAmount(),
                pricingResult.totalDiscountAmount(),
                pricingResult.finalChargeExcludingTax(),
                pricingResult.finalChargeIncludingTax(),
                units,
                remainingQuota,
                normalizeOptionalText(request.note()),
                OffsetDateTime.now()
        );

        subscriptionUtilizationRepository.save(utilization);

        subscriptionAuditService.record(
                allocation.getPlan().getId(),
                allocation.getId(),
                request.userId(),
                request.organizationId(),
                request.groupId(),
                AuditAction.UTILIZATION_RECORDED,
                request.actor(),
                "Recorded utilization for session " + defaultLabel(request.sessionReference(), utilization.getId().toString())
        );

        return toResponse(utilization);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionUtilizationResponse> listByUser(UUID userId) {
        return subscriptionUtilizationRepository.findTop100ByUserIdOrderByUtilizedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private SubscriptionUtilizationResponse toResponse(SubscriptionUtilization utilization) {
        return new SubscriptionUtilizationResponse(
                utilization.getId(),
                utilization.getAllocation().getId(),
                utilization.getPlan().getId(),
                utilization.getPlan().getCode(),
                utilization.getPlan().getName(),
                utilization.getPlan().getCurrencyCode(),
                utilization.getUserId(),
                utilization.getOrganizationId(),
                utilization.getGroupId(),
                utilization.getSessionReference(),
                utilization.getChargingCost(),
                utilization.getSessionFee(),
                utilization.getIdleFee(),
                utilization.getTaxes(),
                utilization.getEligibleSubtotal(),
                utilization.getTotalFeeDiscountAmount(),
                utilization.getSessionFeeDiscountAmount(),
                utilization.getTotalDiscountAmount(),
                utilization.getFinalChargeExcludingTax(),
                utilization.getFinalChargeIncludingTax(),
                utilization.getUnitsConsumed(),
                utilization.getRemainingQuota(),
                utilization.getNote(),
                utilization.getUtilizedAt()
        );
    }

    private SubscriptionAllocation resolveAllocation(UUID allocationId,
                                                     UUID userId,
                                                     UUID organizationId,
                                                     UUID groupId,
                                                     int units) {
        SubscriptionAllocation allocation = allocationId != null
                ? subscriptionAllocationService.requireAllocation(allocationId)
                : resolveBestAllocation(userId, organizationId, groupId);

        OffsetDateTime now = OffsetDateTime.now();
        if (!allocation.isActiveAt(now)) {
            throw new IllegalStateException("Subscription allocation is not active");
        }

        if (!matchesTarget(allocation, userId, organizationId, groupId)) {
            throw new IllegalArgumentException("Requested target does not match the subscription allocation");
        }

        Integer remainingQuota = allocation.getRemainingQuota();
        if (remainingQuota != null && remainingQuota < units) {
            throw new IllegalStateException("Subscription quota exceeded");
        }

        return allocation;
    }

    private SubscriptionAllocation resolveBestAllocation(UUID userId, UUID organizationId, UUID groupId) {
        OffsetDateTime now = OffsetDateTime.now();
        return subscriptionAllocationRepository.findAllWithPlan().stream()
                .filter(allocation -> allocation.isActiveAt(now))
                .filter(allocation -> matchesTarget(allocation, userId, organizationId, groupId))
                .sorted(Comparator
                        .comparingInt(this::priority)
                        .thenComparing(SubscriptionAllocation::getStartsAt, Comparator.reverseOrder()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No active subscription allocation found for requested target"));
    }

    private boolean matchesTarget(SubscriptionAllocation allocation,
                                  UUID userId,
                                  UUID organizationId,
                                  UUID groupId) {
        return switch (allocation.getAllocationType()) {
            case USER -> userId != null && userId.equals(allocation.getUserId());
            case ORGANIZATION -> organizationId != null && organizationId.equals(allocation.getOrganizationId());
            case ORGANIZATION_GROUP -> organizationId != null
                    && groupId != null
                    && organizationId.equals(allocation.getOrganizationId())
                    && groupId.equals(allocation.getGroupId());
        };
    }

    private int priority(SubscriptionAllocation allocation) {
        return switch (allocation.getAllocationType()) {
            case USER -> 0;
            case ORGANIZATION_GROUP -> 1;
            case ORGANIZATION -> 2;
        };
    }

    private PricingResult calculatePricing(SubscriptionPlan plan,
                                           BigDecimal chargingCost,
                                           BigDecimal sessionFee,
                                           BigDecimal idleFee,
                                           BigDecimal taxes) {
        BigDecimal normalizedChargingCost = money(chargingCost);
        BigDecimal normalizedSessionFee = money(sessionFee);
        BigDecimal normalizedIdleFee = money(idleFee);
        BigDecimal normalizedTaxes = money(taxes);

        BigDecimal eligibleSubtotal = money(normalizedChargingCost.add(normalizedSessionFee).add(normalizedIdleFee));
        BigDecimal totalFeeDiscountAmount = discountAmount(
                plan.getTotalFeeDiscountType(),
                plan.getTotalFeeDiscountValue(),
                eligibleSubtotal
        );
        BigDecimal remainingAfterTotalDiscount = money(eligibleSubtotal.subtract(totalFeeDiscountAmount));
        BigDecimal sessionFeeDiscountAmount = discountAmount(
                plan.getSessionFeeDiscountType(),
                plan.getSessionFeeDiscountValue(),
                normalizedSessionFee.min(remainingAfterTotalDiscount)
        );
        BigDecimal totalDiscountAmount = money(totalFeeDiscountAmount.add(sessionFeeDiscountAmount));

        BigDecimal finalChargeExcludingTax = money(eligibleSubtotal.subtract(totalDiscountAmount));
        BigDecimal finalChargeIncludingTax = money(finalChargeExcludingTax.add(normalizedTaxes));

        return new PricingResult(
                normalizedChargingCost,
                normalizedSessionFee,
                normalizedIdleFee,
                normalizedTaxes,
                eligibleSubtotal,
                totalFeeDiscountAmount,
                sessionFeeDiscountAmount,
                totalDiscountAmount,
                finalChargeExcludingTax,
                finalChargeIncludingTax
        );
    }

    private BigDecimal discountAmount(DiscountType discountType, BigDecimal discountValue, BigDecimal baseAmount) {
        BigDecimal normalizedBaseAmount = money(baseAmount);
        BigDecimal normalizedDiscountValue = money(discountValue);
        return switch (discountType) {
            case NONE -> BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
            case PERCENTAGE -> money(normalizedBaseAmount.multiply(normalizedDiscountValue).divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP));
            case FIXED_AMOUNT -> normalizedDiscountValue.min(normalizedBaseAmount);
        };
    }

    private Integer remainingQuotaAfterUse(SubscriptionAllocation allocation, int units) {
        Integer remainingQuota = allocation.getRemainingQuota();
        if (remainingQuota == null) {
            return null;
        }
        return Math.max(0, remainingQuota - units);
    }

    private BigDecimal money(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Fees and taxes cannot be negative");
        }
        return value.setScale(4, RoundingMode.HALF_UP);
    }

    private int normalizeUnits(Integer unitsConsumed) {
        return unitsConsumed == null ? 1 : unitsConsumed;
    }

    private String normalizeOptionalText(String value) {
        String normalized = value == null ? "" : value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private String defaultLabel(String value, String fallback) {
        String normalized = normalizeOptionalText(value);
        return normalized == null ? fallback : normalized;
    }

    private record PricingResult(
            BigDecimal chargingCost,
            BigDecimal sessionFee,
            BigDecimal idleFee,
            BigDecimal taxes,
            BigDecimal eligibleSubtotal,
            BigDecimal totalFeeDiscountAmount,
            BigDecimal sessionFeeDiscountAmount,
            BigDecimal totalDiscountAmount,
            BigDecimal finalChargeExcludingTax,
            BigDecimal finalChargeIncludingTax
    ) {
    }
}
