package com.electrahub.subscription.domain;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionPlan.class);


    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 1024)
    private String description;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "total_fee_discount_type", nullable = false, length = 32)
    private DiscountType totalFeeDiscountType;

    @Column(name = "total_fee_discount_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalFeeDiscountValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_fee_discount_type", nullable = false, length = 32)
    private DiscountType sessionFeeDiscountType;

    @Column(name = "session_fee_discount_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal sessionFeeDiscountValue;

    @Column(name = "default_quota_limit")
    private Integer defaultQuotaLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PlanVisibility visibility = PlanVisibility.ADMIN_ONLY;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_category", nullable = false, length = 32)
    private PlanCategory planCategory = PlanCategory.FLEET;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_model", nullable = false, length = 32)
    private PricingModel pricingModel = PricingModel.FREE;

    @Enumerated(EnumType.STRING)
    @Column(name = "benefit_display_mode", nullable = false, length = 32)
    private BenefitDisplayMode benefitDisplayMode = BenefitDisplayMode.DISCOUNT;

    @Enumerated(EnumType.STRING)
    @Column(name = "quota_unit", nullable = false, length = 32)
    private QuotaUnit quotaUnit = QuotaUnit.SESSION;

    @Column(name = "default_quota_value", precision = 19, scale = 4)
    private BigDecimal defaultQuotaValue;

    @Column(name = "subscription_price_amount", precision = 19, scale = 4)
    private BigDecimal subscriptionPriceAmount;

    @Column(name = "validity_days")
    private Integer validityDays;

    @Column(name = "enterprise_id")
    private UUID enterpriseId;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "public_sort_order")
    private Integer publicSortOrder;

    @Column(name = "allow_stacking", nullable = false)
    private boolean allowStacking;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * Executes subscription plan for `SubscriptionPlan`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     */
    protected SubscriptionPlan() {
        LOGGER.info(" Entering SubscriptionPlan#SubscriptionPlan");
        LOGGER.debug(" Entering SubscriptionPlan#SubscriptionPlan with debug context");
    }

    public SubscriptionPlan(UUID id,
                            String code,
                            String name,
                            String description,
                            String currencyCode,
                            DiscountType totalFeeDiscountType,
                            BigDecimal totalFeeDiscountValue,
                            DiscountType sessionFeeDiscountType,
                            BigDecimal sessionFeeDiscountValue,
                            Integer defaultQuotaLimit,
                            boolean active,
                            OffsetDateTime now) {
        this(
                id,
                code,
                name,
                description,
                currencyCode,
                totalFeeDiscountType,
                totalFeeDiscountValue,
                sessionFeeDiscountType,
                sessionFeeDiscountValue,
                defaultQuotaLimit,
                PlanVisibility.ADMIN_ONLY,
                PlanCategory.FLEET,
                PricingModel.FREE,
                BenefitDisplayMode.DISCOUNT,
                QuotaUnit.SESSION,
                defaultQuotaLimit == null ? null : BigDecimal.valueOf(defaultQuotaLimit),
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                active,
                now
        );
    }

    public SubscriptionPlan(UUID id,
                            String code,
                            String name,
                            String description,
                            String currencyCode,
                            DiscountType totalFeeDiscountType,
                            BigDecimal totalFeeDiscountValue,
                            DiscountType sessionFeeDiscountType,
                            BigDecimal sessionFeeDiscountValue,
                            Integer defaultQuotaLimit,
                            PlanVisibility visibility,
                            PlanCategory planCategory,
                            PricingModel pricingModel,
                            BenefitDisplayMode benefitDisplayMode,
                            QuotaUnit quotaUnit,
                            BigDecimal defaultQuotaValue,
                            BigDecimal subscriptionPriceAmount,
                            Integer validityDays,
                            UUID enterpriseId,
                            String countryCode,
                            Integer publicSortOrder,
                            boolean allowStacking,
                            String createdBy,
                            boolean active,
                            OffsetDateTime now) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.currencyCode = currencyCode;
        this.totalFeeDiscountType = totalFeeDiscountType;
        this.totalFeeDiscountValue = totalFeeDiscountValue;
        this.sessionFeeDiscountType = sessionFeeDiscountType;
        this.sessionFeeDiscountValue = sessionFeeDiscountValue;
        this.defaultQuotaLimit = defaultQuotaLimit;
        this.visibility = visibility == null ? PlanVisibility.ADMIN_ONLY : visibility;
        this.planCategory = planCategory == null ? PlanCategory.FLEET : planCategory;
        this.pricingModel = pricingModel == null ? PricingModel.FREE : pricingModel;
        this.benefitDisplayMode = benefitDisplayMode == null ? BenefitDisplayMode.DISCOUNT : benefitDisplayMode;
        this.quotaUnit = quotaUnit == null ? QuotaUnit.SESSION : quotaUnit;
        this.defaultQuotaValue = defaultQuotaValue;
        this.subscriptionPriceAmount = subscriptionPriceAmount;
        this.validityDays = validityDays;
        this.enterpriseId = enterpriseId;
        this.countryCode = countryCode;
        this.publicSortOrder = publicSortOrder;
        this.allowStacking = allowStacking;
        this.createdBy = createdBy;
        this.active = active;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Executes pre update for `SubscriptionPlan`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     */
    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Retrieves get id for `SubscriptionPlan`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getId.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Retrieves get code for `SubscriptionPlan`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getCode.
     */
    public String getCode() {
        return code;
    }

    /**
     * Retrieves get name for `SubscriptionPlan`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getName.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves get description for `SubscriptionPlan`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getDescription.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Retrieves get currency code for `SubscriptionPlan`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getCurrencyCode.
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * Retrieves get total fee discount type for `SubscriptionPlan`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getTotalFeeDiscountType.
     */
    public DiscountType getTotalFeeDiscountType() {
        return totalFeeDiscountType;
    }

    /**
     * Retrieves get total fee discount value for `SubscriptionPlan`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getTotalFeeDiscountValue.
     */
    public BigDecimal getTotalFeeDiscountValue() {
        return totalFeeDiscountValue;
    }

    /**
     * Retrieves get session fee discount type for `SubscriptionPlan`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getSessionFeeDiscountType.
     */
    public DiscountType getSessionFeeDiscountType() {
        return sessionFeeDiscountType;
    }

    /**
     * Retrieves get session fee discount value for `SubscriptionPlan`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getSessionFeeDiscountValue.
     */
    public BigDecimal getSessionFeeDiscountValue() {
        return sessionFeeDiscountValue;
    }

    /**
     * Retrieves get default quota limit for `SubscriptionPlan`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getDefaultQuotaLimit.
     */
    public Integer getDefaultQuotaLimit() {
        return defaultQuotaLimit;
    }

    public PlanVisibility getVisibility() {
        return visibility;
    }

    public PlanCategory getPlanCategory() {
        return planCategory;
    }

    public PricingModel getPricingModel() {
        return pricingModel;
    }

    public BenefitDisplayMode getBenefitDisplayMode() {
        return benefitDisplayMode;
    }

    public QuotaUnit getQuotaUnit() {
        return quotaUnit;
    }

    public BigDecimal getDefaultQuotaValue() {
        return defaultQuotaValue;
    }

    public BigDecimal getEffectiveDefaultQuotaValue() {
        if (defaultQuotaValue != null) {
            return defaultQuotaValue;
        }
        return defaultQuotaLimit == null ? null : BigDecimal.valueOf(defaultQuotaLimit);
    }

    public BigDecimal getSubscriptionPriceAmount() {
        return subscriptionPriceAmount;
    }

    public Integer getValidityDays() {
        return validityDays;
    }

    public UUID getEnterpriseId() {
        return enterpriseId;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public Integer getPublicSortOrder() {
        return publicSortOrder;
    }

    public boolean isAllowStacking() {
        return allowStacking;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Executes is active for `SubscriptionPlan`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by isActive.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Retrieves get created at for `SubscriptionPlan`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getCreatedAt.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Retrieves get updated at for `SubscriptionPlan`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getUpdatedAt.
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Updates set active for `SubscriptionPlan`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @param active input consumed by setActive.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    public void updateDetails(String name,
                              String description,
                              String currencyCode,
                              DiscountType totalFeeDiscountType,
                              BigDecimal totalFeeDiscountValue,
                              DiscountType sessionFeeDiscountType,
                              BigDecimal sessionFeeDiscountValue,
                              Integer defaultQuotaLimit,
                              PlanVisibility visibility,
                              PlanCategory planCategory,
                              PricingModel pricingModel,
                              BenefitDisplayMode benefitDisplayMode,
                              QuotaUnit quotaUnit,
                              BigDecimal defaultQuotaValue,
                              BigDecimal subscriptionPriceAmount,
                              Integer validityDays,
                              UUID enterpriseId,
                              String countryCode,
                              Integer publicSortOrder,
                              boolean allowStacking,
                              boolean active) {
        this.name = name;
        this.description = description;
        this.currencyCode = currencyCode;
        this.totalFeeDiscountType = totalFeeDiscountType;
        this.totalFeeDiscountValue = totalFeeDiscountValue;
        this.sessionFeeDiscountType = sessionFeeDiscountType;
        this.sessionFeeDiscountValue = sessionFeeDiscountValue;
        this.defaultQuotaLimit = defaultQuotaLimit;
        this.visibility = visibility == null ? PlanVisibility.ADMIN_ONLY : visibility;
        this.planCategory = planCategory == null ? PlanCategory.FLEET : planCategory;
        this.pricingModel = pricingModel == null ? PricingModel.FREE : pricingModel;
        this.benefitDisplayMode = benefitDisplayMode == null ? BenefitDisplayMode.DISCOUNT : benefitDisplayMode;
        this.quotaUnit = quotaUnit == null ? QuotaUnit.SESSION : quotaUnit;
        this.defaultQuotaValue = defaultQuotaValue;
        this.subscriptionPriceAmount = subscriptionPriceAmount;
        this.validityDays = validityDays;
        this.enterpriseId = enterpriseId;
        this.countryCode = countryCode;
        this.publicSortOrder = publicSortOrder;
        this.allowStacking = allowStacking;
        this.active = active;
    }
}
