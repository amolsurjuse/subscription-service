package com.electrahub.subscription.service;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.electrahub.subscription.api.dto.CreateSubscriptionPlanRequest;
import com.electrahub.subscription.api.dto.SubscriptionPlanResponse;
import com.electrahub.subscription.api.dto.SubscriptionPlanSearchResponse;
import com.electrahub.subscription.api.error.ConflictException;
import com.electrahub.subscription.api.error.NotFoundException;
import com.electrahub.subscription.domain.AuditAction;
import com.electrahub.subscription.domain.BenefitDisplayMode;
import com.electrahub.subscription.domain.DiscountType;
import com.electrahub.subscription.domain.PlanCategory;
import com.electrahub.subscription.domain.PlanVisibility;
import com.electrahub.subscription.domain.PricingModel;
import com.electrahub.subscription.domain.QuotaUnit;
import com.electrahub.subscription.domain.SubscriptionPlan;
import com.electrahub.subscription.repository.SubscriptionPlanRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class SubscriptionPlanService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionPlanService.class);


    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionAuditService subscriptionAuditService;

    public SubscriptionPlanService(SubscriptionPlanRepository subscriptionPlanRepository,
                                   SubscriptionAuditService subscriptionAuditService) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.subscriptionAuditService = subscriptionAuditService;
    }

    /**
     * Creates create for `SubscriptionPlanService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param request input consumed by create.
     * @return result produced by create.
     */
    @Transactional
    public SubscriptionPlanResponse create(CreateSubscriptionPlanRequest request) {
        LOGGER.info(" Entering SubscriptionPlanService#create");
        LOGGER.debug(" Entering SubscriptionPlanService#create with debug context");
        String normalizedCode = normalizeCode(request.code());
        if (subscriptionPlanRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new ConflictException("Subscription plan already exists for code " + normalizedCode);
        }

        validateDiscount(request.totalFeeDiscountType(), request.totalFeeDiscountValue(), "total fee");
        validateDiscount(request.sessionFeeDiscountType(), request.sessionFeeDiscountValue(), "session fee");

        OffsetDateTime now = OffsetDateTime.now();
        SubscriptionPlan plan = new SubscriptionPlan(
                UUID.randomUUID(),
                normalizedCode,
                normalizeText(request.name()),
                normalizeOptionalText(request.description()),
                normalizeCurrency(request.currencyCode()),
                request.totalFeeDiscountType(),
                normalizeMoney(request.totalFeeDiscountValue()),
                request.sessionFeeDiscountType(),
                normalizeMoney(request.sessionFeeDiscountValue()),
                request.defaultQuotaLimit(),
                defaultValue(request.visibility(), PlanVisibility.ADMIN_ONLY),
                defaultValue(request.planCategory(), PlanCategory.FLEET),
                defaultValue(request.pricingModel(), PricingModel.FREE),
                defaultValue(request.benefitDisplayMode(), BenefitDisplayMode.DISCOUNT),
                defaultValue(request.quotaUnit(), QuotaUnit.SESSION),
                normalizeOptionalDecimal(request.defaultQuotaValue(), request.defaultQuotaLimit()),
                normalizeOptionalMoney(request.subscriptionPriceAmount()),
                request.validityDays(),
                request.enterpriseId(),
                normalizeCountry(request.countryCode()),
                request.publicSortOrder(),
                Boolean.TRUE.equals(request.allowStacking()),
                normalizeOptionalText(request.createdBy()),
                true,
                now
        );
        subscriptionPlanRepository.save(plan);

        subscriptionAuditService.record(
                plan.getId(),
                null,
                null,
                null,
                null,
                AuditAction.PLAN_CREATED,
                defaultLabel(request.createdBy(), "system"),
                "Created subscription plan " + plan.getCode()
        );

        return toResponse(plan);
    }

    /**
     * Retrieves list for `SubscriptionPlanService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param limit input consumed by list.
     * @param offset input consumed by list.
     * @return result produced by list.
     */
    @Transactional(readOnly = true)
    public SubscriptionPlanSearchResponse list(int limit,
                                               int offset,
                                               String query,
                                               PlanVisibility visibility,
                                               PlanCategory planCategory,
                                               PricingModel pricingModel,
                                               BenefitDisplayMode benefitDisplayMode,
                                               QuotaUnit quotaUnit,
                                               UUID enterpriseId,
                                               String countryCode,
                                               Boolean active) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        int safeOffset = Math.max(0, offset);
        int page = safeOffset / safeLimit;

        String normalizedQuery = normalizeOptionalText(query);
        boolean queryEmpty = normalizedQuery == null;
        String queryPattern = queryEmpty ? "" : "%" + normalizedQuery.toLowerCase(Locale.ROOT) + "%";
        String normalizedCountry = normalizeCountry(countryCode);
        boolean countryEmpty = normalizedCountry == null;

        var pageResult = subscriptionPlanRepository.searchPaged(
                queryEmpty,
                queryPattern,
                visibility,
                planCategory,
                pricingModel,
                benefitDisplayMode,
                quotaUnit,
                enterpriseId,
                countryEmpty,
                normalizedCountry,
                active,
                PageRequest.of(page, safeLimit, Sort.by(Sort.Direction.DESC, "updatedAt"))
        );
        var items = pageResult.getContent().stream()
                .map(this::toResponse)
                .toList();
        long total = pageResult.getTotalElements();
        int totalPages = Math.max(pageResult.getTotalPages(), total > 0 ? 1 : 0);

        return new SubscriptionPlanSearchResponse(items, total, safeLimit, safeOffset, page, totalPages, pageResult.hasNext(), pageResult.hasPrevious());
    }

    /**
     * Retrieves get for `SubscriptionPlanService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param planId input consumed by get.
     * @return result produced by get.
     */
    @Transactional(readOnly = true)
    public SubscriptionPlanResponse get(UUID planId) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("Subscription plan not found: " + planId));
        return toResponse(plan);
    }

    @Transactional
    public SubscriptionPlanResponse update(UUID planId, CreateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("Subscription plan not found: " + planId));

        validateDiscount(request.totalFeeDiscountType(), request.totalFeeDiscountValue(), "total fee");
        validateDiscount(request.sessionFeeDiscountType(), request.sessionFeeDiscountValue(), "session fee");

        plan.updateDetails(
                normalizeText(request.name()),
                normalizeOptionalText(request.description()),
                normalizeCurrency(request.currencyCode()),
                request.totalFeeDiscountType(),
                normalizeMoney(request.totalFeeDiscountValue()),
                request.sessionFeeDiscountType(),
                normalizeMoney(request.sessionFeeDiscountValue()),
                request.defaultQuotaLimit(),
                defaultValue(request.visibility(), plan.getVisibility()),
                defaultValue(request.planCategory(), plan.getPlanCategory()),
                defaultValue(request.pricingModel(), plan.getPricingModel()),
                defaultValue(request.benefitDisplayMode(), plan.getBenefitDisplayMode()),
                defaultValue(request.quotaUnit(), plan.getQuotaUnit()),
                normalizeOptionalDecimal(request.defaultQuotaValue(), request.defaultQuotaLimit()),
                normalizeOptionalMoney(request.subscriptionPriceAmount()),
                request.validityDays(),
                request.enterpriseId(),
                normalizeCountry(request.countryCode()),
                request.publicSortOrder(),
                Boolean.TRUE.equals(request.allowStacking()),
                request.active() == null || request.active()
        );

        subscriptionAuditService.record(
                plan.getId(),
                null,
                null,
                null,
                null,
                AuditAction.PLAN_UPDATED,
                defaultLabel(request.createdBy(), "system"),
                "Updated subscription plan " + plan.getCode()
        );

        return toResponse(plan);
    }

    /**
     * Executes require plan for `SubscriptionPlanService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param planId input consumed by requirePlan.
     * @return result produced by requirePlan.
     */
    SubscriptionPlan requirePlan(UUID planId) {
        return subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("Subscription plan not found: " + planId));
    }

    Optional<SubscriptionPlan> findByCode(String code) {
        return subscriptionPlanRepository.findByCodeIgnoreCase(code);
    }

    /**
     * Executes to response for `SubscriptionPlanService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param plan input consumed by toResponse.
     * @return result produced by toResponse.
     */
    private SubscriptionPlanResponse toResponse(SubscriptionPlan plan) {
        return new SubscriptionPlanResponse(
                plan.getId(),
                plan.getCode(),
                plan.getName(),
                plan.getDescription(),
                plan.getCurrencyCode(),
                plan.getTotalFeeDiscountType(),
                plan.getTotalFeeDiscountValue(),
                plan.getSessionFeeDiscountType(),
                plan.getSessionFeeDiscountValue(),
                plan.getDefaultQuotaLimit(),
                plan.getVisibility(),
                plan.getPlanCategory(),
                plan.getPricingModel(),
                plan.getBenefitDisplayMode(),
                plan.getQuotaUnit(),
                plan.getEffectiveDefaultQuotaValue(),
                plan.getSubscriptionPriceAmount(),
                plan.getValidityDays(),
                plan.getEnterpriseId(),
                plan.getCountryCode(),
                plan.getPublicSortOrder(),
                plan.isAllowStacking(),
                plan.getCreatedBy(),
                plan.isActive(),
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }

    /**
     * Validates validate discount for `SubscriptionPlanService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param discountType input consumed by validateDiscount.
     * @param value input consumed by validateDiscount.
     * @param label input consumed by validateDiscount.
     */
    private void validateDiscount(DiscountType discountType, BigDecimal value, String label) {
        BigDecimal normalizedValue = normalizeMoney(value);
        if (discountType == DiscountType.NONE && normalizedValue.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException(label + " discount value must be zero when type is NONE");
        }
        if (discountType == DiscountType.PERCENTAGE && normalizedValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException(label + " percentage discount cannot exceed 100");
        }
    }

    /**
     * Executes normalize money for `SubscriptionPlanService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param value input consumed by normalizeMoney.
     * @return result produced by normalizeMoney.
     */
    private BigDecimal normalizeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.stripTrailingZeros().max(BigDecimal.ZERO);
    }

    private BigDecimal normalizeOptionalMoney(BigDecimal value) {
        return value == null ? null : normalizeMoney(value);
    }

    private BigDecimal normalizeOptionalDecimal(BigDecimal value, Integer fallbackInteger) {
        if (value != null) {
            return value.stripTrailingZeros().max(BigDecimal.ZERO);
        }
        return fallbackInteger == null ? null : BigDecimal.valueOf(fallbackInteger);
    }

    /**
     * Executes normalize code for `SubscriptionPlanService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param code input consumed by normalizeCode.
     * @return result produced by normalizeCode.
     */
    private String normalizeCode(String code) {
        return normalizeText(code).toUpperCase(Locale.ROOT);
    }

    /**
     * Executes normalize currency for `SubscriptionPlanService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param currencyCode input consumed by normalizeCurrency.
     * @return result produced by normalizeCurrency.
     */
    private String normalizeCurrency(String currencyCode) {
        return normalizeText(currencyCode).toUpperCase(Locale.ROOT);
    }

    private String normalizeCountry(String countryCode) {
        String normalized = normalizeOptionalText(countryCode);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    /**
     * Executes normalize text for `SubscriptionPlanService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param value input consumed by normalizeText.
     * @return result produced by normalizeText.
     */
    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Executes normalize optional text for `SubscriptionPlanService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param value input consumed by normalizeOptionalText.
     * @return result produced by normalizeOptionalText.
     */
    private String normalizeOptionalText(String value) {
        String normalized = normalizeText(value);
        return normalized.isBlank() ? null : normalized;
    }

    private <T> T defaultValue(T value, T fallback) {
        return value == null ? fallback : value;
    }

    private String defaultLabel(String value, String fallback) {
        String normalized = normalizeOptionalText(value);
        return normalized == null ? fallback : normalized;
    }
}
