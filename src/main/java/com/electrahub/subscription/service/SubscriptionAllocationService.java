package com.electrahub.subscription.service;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.electrahub.subscription.api.dto.CreateSubscriptionAllocationRequest;
import com.electrahub.subscription.api.dto.CreateSubscriptionGrantRequest;
import com.electrahub.subscription.api.dto.DriverSubscriptionPlanResponse;
import com.electrahub.subscription.api.dto.DriverSubscriptionResponse;
import com.electrahub.subscription.api.dto.PagedResponse;
import com.electrahub.subscription.api.dto.SubscriptionAllocationResponse;
import com.electrahub.subscription.api.dto.SubscriptionPlanResponse;
import com.electrahub.subscription.api.dto.UpdateAllocationStatusRequest;
import com.electrahub.subscription.api.error.NotFoundException;
import com.electrahub.subscription.domain.AllocationStatus;
import com.electrahub.subscription.domain.AllocationSource;
import com.electrahub.subscription.domain.AllocationType;
import com.electrahub.subscription.domain.AuditAction;
import com.electrahub.subscription.domain.PlanCategory;
import com.electrahub.subscription.domain.PlanVisibility;
import com.electrahub.subscription.domain.SubscriptionAllocation;
import com.electrahub.subscription.domain.SubscriptionPlan;
import com.electrahub.subscription.repository.SubscriptionAllocationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionAllocationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionAllocationService.class);
    private static final String NEW_USER_PROMO_PLAN_CODE = "NEW_USER_20_OFF_500KWH_1Y";


    private final SubscriptionAllocationRepository subscriptionAllocationRepository;
    private final SubscriptionPlanService subscriptionPlanService;
    private final SubscriptionAuditService subscriptionAuditService;

    public SubscriptionAllocationService(SubscriptionAllocationRepository subscriptionAllocationRepository,
                                         SubscriptionPlanService subscriptionPlanService,
                                         SubscriptionAuditService subscriptionAuditService) {
        this.subscriptionAllocationRepository = subscriptionAllocationRepository;
        this.subscriptionPlanService = subscriptionPlanService;
        this.subscriptionAuditService = subscriptionAuditService;
    }

    /**
     * Creates create for `SubscriptionAllocationService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param request input consumed by create.
     * @return result produced by create.
     */
    @Transactional
    public SubscriptionAllocationResponse create(CreateSubscriptionAllocationRequest request) {
        LOGGER.info(" Entering SubscriptionAllocationService#create");
        LOGGER.debug(" Entering SubscriptionAllocationService#create with debug context");
        validateTarget(request);
        validateSchedule(request.startsAt(), request.endsAt());

        SubscriptionPlan plan = subscriptionPlanService.requirePlan(request.planId());
        AllocationStatus status = request.status() == null ? AllocationStatus.ACTIVE : request.status();
        OffsetDateTime now = OffsetDateTime.now();
        BigDecimal quotaLimitValue = request.quotaLimitValue() != null
                ? request.quotaLimitValue()
                : request.quotaLimit() == null ? null : BigDecimal.valueOf(request.quotaLimit());

        SubscriptionAllocation allocation = new SubscriptionAllocation(
                UUID.randomUUID(),
                plan,
                request.allocationType(),
                request.userId(),
                request.organizationId(),
                request.groupId(),
                request.quotaLimit(),
                quotaLimitValue,
                request.startsAt(),
                request.endsAt(),
                status,
                normalizeActor(request.createdBy()),
                request.source(),
                normalizeOptionalText(request.sourceLabel()),
                normalizeOptionalText(request.grantReason()),
                normalizeOptionalText(request.externalReference()),
                normalizeOptionalText(request.vin()),
                request.enterpriseId(),
                now
        );
        subscriptionAllocationRepository.save(allocation);

        subscriptionAuditService.record(
                plan.getId(),
                allocation.getId(),
                allocation.getUserId(),
                allocation.getOrganizationId(),
                allocation.getGroupId(),
                allocation.getSource() == AllocationSource.ADMIN_GRANTED || allocation.getSource() == AllocationSource.OEM_GRANTED
                        ? AuditAction.ALLOCATION_GRANTED
                        : AuditAction.ALLOCATION_CREATED,
                allocation.getCreatedBy(),
                buildCreateDetail(allocation)
        );

        return toResponse(allocation);
    }

    @Transactional
    public SubscriptionAllocationResponse grantToUser(CreateSubscriptionGrantRequest request) {
        SubscriptionPlan plan = subscriptionPlanService.requirePlan(request.planId());
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime startsAt = request.startsAt() == null ? now : request.startsAt();
        OffsetDateTime endsAt = request.endsAt();
        if (endsAt == null && plan.getValidityDays() != null) {
            endsAt = startsAt.plusDays(plan.getValidityDays());
        }
        validateSchedule(startsAt, endsAt);

        AllocationSource source = request.source() != null
                ? request.source()
                : plan.getPlanCategory() == PlanCategory.OEM_PROMOTION ? AllocationSource.OEM_GRANTED : AllocationSource.ADMIN_GRANTED;
        String sourceLabel = source == AllocationSource.OEM_GRANTED ? "OEM grant" : "Admin grant";
        if (request.dealerCode() != null && !request.dealerCode().isBlank()) {
            sourceLabel = sourceLabel + " - " + request.dealerCode().trim();
        }

        SubscriptionAllocation allocation = new SubscriptionAllocation(
                UUID.randomUUID(),
                plan,
                AllocationType.USER,
                request.userId(),
                null,
                null,
                request.quotaValue().intValue(),
                request.quotaValue(),
                startsAt,
                endsAt,
                AllocationStatus.ACTIVE,
                normalizeActor(request.createdBy()),
                source,
                sourceLabel,
                normalizeOptionalText(request.grantReason()),
                normalizeOptionalText(request.externalReference()),
                normalizeOptionalText(request.vin()),
                request.enterpriseId() != null ? request.enterpriseId() : plan.getEnterpriseId(),
                now
        );
        subscriptionAllocationRepository.save(allocation);

        subscriptionAuditService.record(
                plan.getId(),
                allocation.getId(),
                allocation.getUserId(),
                null,
                null,
                AuditAction.ALLOCATION_GRANTED,
                allocation.getCreatedBy(),
                "Granted " + request.quotaValue().stripTrailingZeros().toPlainString()
                        + " " + (request.quotaUnit() == null ? plan.getQuotaUnit() : request.quotaUnit())
                        + " using plan " + plan.getCode()
        );

        return toResponse(allocation);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionAllocationResponse> list(UUID userId,
                                                     UUID organizationId,
                                                     UUID groupId,
                                                     Boolean activeOnly) {
        OffsetDateTime now = OffsetDateTime.now();
        return subscriptionAllocationRepository.findAllWithPlan().stream()
                .filter(allocation -> userId == null || userId.equals(allocation.getUserId()))
                .filter(allocation -> organizationId == null || organizationId.equals(allocation.getOrganizationId()))
                .filter(allocation -> groupId == null || groupId.equals(allocation.getGroupId()))
                .filter(allocation -> !Boolean.TRUE.equals(activeOnly) || allocation.isActiveAt(now))
                .sorted(Comparator.comparing(SubscriptionAllocation::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<SubscriptionAllocationResponse> listPaged(UUID userId,
                                                                   UUID organizationId,
                                                                   UUID groupId,
                                                                   UUID planId,
                                                                   AllocationType allocationType,
                                                                   AllocationStatus status,
                                                                   AllocationSource source,
                                                                   UUID enterpriseId,
                                                                   Boolean exhausted,
                                                                   Boolean activeOnly,
                                                                   int limit,
                                                                   int offset) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        int safeOffset = Math.max(0, offset);
        int page = safeOffset / safeLimit;
        boolean activeFilter = Boolean.TRUE.equals(activeOnly);

        var pageable = PageRequest.of(page, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt"));
        var pageResult = subscriptionAllocationRepository.searchPaged(
                userId,
                organizationId,
                groupId,
                planId,
                allocationType,
                status,
                source,
                enterpriseId,
                exhausted,
                activeFilter,
                OffsetDateTime.now(),
                pageable
        );

        var items = pageResult.getContent().stream().map(this::toResponse).toList();
        long total = pageResult.getTotalElements();
        int totalPages = Math.max(pageResult.getTotalPages(), total > 0 ? 1 : 0);

        return new PagedResponse<>(
                items,
                total,
                safeLimit,
                safeOffset,
                page,
                totalPages,
                pageResult.hasNext(),
                pageResult.hasPrevious()
        );
    }

    @Transactional
    public List<DriverSubscriptionResponse> listDriverSubscriptions(UUID userId,
                                                                    boolean activeOnly,
                                                                    int limit,
                                                                    int offset) {
        ensureNewUserPromotion(userId);
        int safeLimit = Math.max(1, Math.min(limit, 100));
        int safeOffset = Math.max(0, offset);
        OffsetDateTime now = OffsetDateTime.now();

        return subscriptionAllocationRepository.findAllWithPlan().stream()
                .filter(allocation -> userId.equals(allocation.getUserId()))
                .filter(allocation -> !activeOnly || allocation.isActiveAt(now))
                .sorted(Comparator.comparing(SubscriptionAllocation::getCreatedAt).reversed())
                .skip(safeOffset)
                .limit(safeLimit)
                .map(this::toDriverResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DriverSubscriptionPlanResponse> listDriverPlans(String countryCode,
                                                                String currency,
                                                                int limit,
                                                                int offset) {
        String normalizedCountry = normalizeOptionalText(countryCode);
        String normalizedCurrency = normalizeOptionalText(currency);
        return subscriptionPlanService.list(
                        Math.max(1, Math.min(limit, 100)),
                        Math.max(0, offset),
                        null,
                        PlanVisibility.PUBLIC,
                        PlanCategory.DRIVER_PUBLIC,
                        null,
                        null,
                        null,
                        null,
                        normalizedCountry,
                        true
                )
                .items()
                .stream()
                .filter(plan -> normalizedCurrency == null || normalizedCurrency.equalsIgnoreCase(plan.currencyCode()))
                .map(this::toDriverPlanResponse)
                .toList();
    }

    @Transactional
    public SubscriptionAllocation ensureNewUserPromotion(UUID userId) {
        if (userId == null) {
            return null;
        }

        OffsetDateTime now = OffsetDateTime.now();
        var existing = subscriptionAllocationRepository.findAllWithPlan().stream()
                .filter(allocation -> userId.equals(allocation.getUserId()))
                .filter(allocation -> NEW_USER_PROMO_PLAN_CODE.equalsIgnoreCase(allocation.getPlan().getCode()))
                .max(Comparator.comparing(SubscriptionAllocation::getCreatedAt));
        if (existing.isPresent()) {
            return existing.get();
        }

        return subscriptionPlanService.findByCode(NEW_USER_PROMO_PLAN_CODE)
                .filter(SubscriptionPlan::isActive)
                .map(plan -> createNewUserPromotionAllocation(userId, plan, now))
                .orElse(null);
    }

    /**
     * Updates update status for `SubscriptionAllocationService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param allocationId input consumed by updateStatus.
     * @param request input consumed by updateStatus.
     * @return result produced by updateStatus.
     */
    @Transactional
    public SubscriptionAllocationResponse updateStatus(UUID allocationId, UpdateAllocationStatusRequest request) {
        SubscriptionAllocation allocation = requireAllocation(allocationId);
        allocation.setStatus(request.status());

        subscriptionAuditService.record(
                allocation.getPlan().getId(),
                allocation.getId(),
                allocation.getUserId(),
                allocation.getOrganizationId(),
                allocation.getGroupId(),
                AuditAction.ALLOCATION_STATUS_CHANGED,
                normalizeActor(request.actor()),
                buildStatusDetail(request)
        );

        return toResponse(allocation);
    }

    /**
     * Executes require allocation for `SubscriptionAllocationService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param allocationId input consumed by requireAllocation.
     * @return result produced by requireAllocation.
     */
    @Transactional(readOnly = true)
    public SubscriptionAllocation requireAllocation(UUID allocationId) {
        return subscriptionAllocationRepository.findDetailedById(allocationId)
                .orElseThrow(() -> new NotFoundException("Subscription allocation not found: " + allocationId));
    }

    /**
     * Executes to response for `SubscriptionAllocationService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param allocation input consumed by toResponse.
     * @return result produced by toResponse.
     */
    private SubscriptionAllocationResponse toResponse(SubscriptionAllocation allocation) {
        return new SubscriptionAllocationResponse(
                allocation.getId(),
                allocation.getPlan().getId(),
                allocation.getPlan().getCode(),
                allocation.getPlan().getName(),
                allocation.getPlan().getCurrencyCode(),
                allocation.getAllocationType(),
                allocation.getUserId(),
                allocation.getOrganizationId(),
                allocation.getGroupId(),
                allocation.getQuotaLimit(),
                allocation.getQuotaLimitValue(),
                allocation.getEffectiveQuotaLimit(),
                allocation.getEffectiveQuotaLimitValue(),
                allocation.getConsumedUnits(),
                allocation.getConsumedValue(),
                allocation.getRemainingQuota(),
                allocation.getRemainingQuotaValue(),
                allocation.getPlan().getQuotaUnit(),
                allocation.getPlan().getPricingModel(),
                allocation.getPlan().getBenefitDisplayMode(),
                allocation.getStartsAt(),
                allocation.getEndsAt(),
                allocation.getStatus(),
                allocation.getCreatedBy(),
                allocation.getSource(),
                allocation.getSourceLabel(),
                allocation.getGrantReason(),
                allocation.getExternalReference(),
                allocation.getVin(),
                allocation.getEnterpriseId(),
                allocation.getLastUsedAt(),
                allocation.getCreatedAt(),
                allocation.getUpdatedAt()
        );
    }

    private DriverSubscriptionResponse toDriverResponse(SubscriptionAllocation allocation) {
        return new DriverSubscriptionResponse(
                allocation.getId(),
                allocation.getPlan().getId(),
                allocation.getPlan().getCode(),
                allocation.getPlan().getName(),
                allocation.getSource().name(),
                allocation.getSourceLabel(),
                allocation.getPlan().getPricingModel(),
                allocation.getPlan().getBenefitDisplayMode(),
                allocation.getPlan().getQuotaUnit(),
                allocation.getEffectiveQuotaLimitValue(),
                allocation.getConsumedValue(),
                allocation.getRemainingQuotaValue(),
                allocation.getStartsAt(),
                allocation.getEndsAt(),
                allocation.getStatus()
        );
    }

    private DriverSubscriptionPlanResponse toDriverPlanResponse(SubscriptionPlanResponse plan) {
        return new DriverSubscriptionPlanResponse(
                plan.id(),
                plan.code(),
                plan.name(),
                plan.description(),
                plan.currencyCode(),
                plan.planCategory(),
                plan.pricingModel(),
                plan.benefitDisplayMode(),
                plan.quotaUnit(),
                plan.defaultQuotaValue(),
                plan.validityDays(),
                plan.totalFeeDiscountType(),
                plan.totalFeeDiscountValue(),
                plan.sessionFeeDiscountType(),
                plan.sessionFeeDiscountValue(),
                plan.active()
        );
    }

    private SubscriptionAllocation createNewUserPromotionAllocation(UUID userId, SubscriptionPlan plan, OffsetDateTime now) {
        OffsetDateTime startsAt = now;
        OffsetDateTime endsAt = plan.getValidityDays() == null ? null : startsAt.plusDays(plan.getValidityDays());
        BigDecimal quota = plan.getEffectiveDefaultQuotaValue();
        SubscriptionAllocation allocation = new SubscriptionAllocation(
                UUID.randomUUID(),
                plan,
                AllocationType.USER,
                userId,
                null,
                null,
                quota == null ? null : quota.intValue(),
                quota,
                startsAt,
                endsAt,
                AllocationStatus.ACTIVE,
                "system",
                AllocationSource.OEM_GRANTED,
                "New user promotion",
                "Automatic 20% charging discount for new drivers",
                "new-user-promo:" + userId,
                null,
                plan.getEnterpriseId(),
                now
        );
        subscriptionAllocationRepository.save(allocation);
        subscriptionAuditService.record(
                plan.getId(),
                allocation.getId(),
                userId,
                null,
                null,
                AuditAction.ALLOCATION_GRANTED,
                "system",
                "Automatically granted new user charging promotion"
        );
        return allocation;
    }

    /**
     * Validates validate target for `SubscriptionAllocationService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param request input consumed by validateTarget.
     */
    private void validateTarget(CreateSubscriptionAllocationRequest request) {
        switch (request.allocationType()) {
            case USER -> {
                if (request.userId() == null) {
                    throw new IllegalArgumentException("userId is required for USER allocations");
                }
            }
            case ORGANIZATION -> {
                if (request.organizationId() == null) {
                    throw new IllegalArgumentException("organizationId is required for ORGANIZATION allocations");
                }
            }
            case ORGANIZATION_GROUP -> {
                if (request.organizationId() == null || request.groupId() == null) {
                    throw new IllegalArgumentException("organizationId and groupId are required for ORGANIZATION_GROUP allocations");
                }
            }
        }
    }

    /**
     * Validates validate schedule for `SubscriptionAllocationService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param startsAt input consumed by validateSchedule.
     * @param endsAt input consumed by validateSchedule.
     */
    private void validateSchedule(OffsetDateTime startsAt, OffsetDateTime endsAt) {
        if (endsAt != null && endsAt.isBefore(startsAt)) {
            throw new IllegalArgumentException("Allocation end time must be after start time");
        }
    }

    /**
     * Creates build status detail for `SubscriptionAllocationService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param request input consumed by buildStatusDetail.
     * @return result produced by buildStatusDetail.
     */
    private String buildStatusDetail(UpdateAllocationStatusRequest request) {
        String reason = request.reason() == null ? "" : request.reason().trim();
        if (reason.isBlank()) {
            return "Changed allocation status to " + request.status();
        }
        return "Changed allocation status to " + request.status() + ": " + reason;
    }

    private String buildCreateDetail(SubscriptionAllocation allocation) {
        if (allocation.getSource() == AllocationSource.ADMIN_GRANTED || allocation.getSource() == AllocationSource.OEM_GRANTED) {
            return "Granted subscription allocation " + allocation.getId() + " for plan " + allocation.getPlan().getCode();
        }
        return "Created subscription allocation " + allocation.getId();
    }

    /**
     * Executes normalize actor for `SubscriptionAllocationService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param actor input consumed by normalizeActor.
     * @return result produced by normalizeActor.
     */
    private String normalizeActor(String actor) {
        String normalized = actor == null ? "" : actor.trim();
        return normalized.isBlank() ? "system" : normalized;
    }

    private String normalizeOptionalText(String value) {
        String normalized = value == null ? "" : value.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
