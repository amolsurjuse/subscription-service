package com.electrahub.subscription.domain;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscription_utilizations")
public class SubscriptionUtilization {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionUtilization.class);


    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "allocation_id", nullable = false)
    private SubscriptionAllocation allocation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "session_reference", length = 120)
    private String sessionReference;

    @Column(name = "charging_cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal chargingCost;

    @Column(name = "session_fee", nullable = false, precision = 19, scale = 4)
    private BigDecimal sessionFee;

    @Column(name = "idle_fee", nullable = false, precision = 19, scale = 4)
    private BigDecimal idleFee;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal taxes;

    @Column(name = "eligible_subtotal", nullable = false, precision = 19, scale = 4)
    private BigDecimal eligibleSubtotal;

    @Column(name = "total_fee_discount_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalFeeDiscountAmount;

    @Column(name = "session_fee_discount_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal sessionFeeDiscountAmount;

    @Column(name = "total_discount_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalDiscountAmount;

    @Column(name = "final_charge_excluding_tax", nullable = false, precision = 19, scale = 4)
    private BigDecimal finalChargeExcludingTax;

    @Column(name = "final_charge_including_tax", nullable = false, precision = 19, scale = 4)
    private BigDecimal finalChargeIncludingTax;

    @Column(name = "units_consumed", nullable = false)
    private int unitsConsumed;

    @Column(name = "energy_kwh", precision = 19, scale = 4)
    private BigDecimal energyKwh;

    @Column(name = "quota_unit", nullable = false, length = 32)
    private String quotaUnit = QuotaUnit.SESSION.name();

    @Column(name = "quota_consumed_value", precision = 19, scale = 4)
    private BigDecimal quotaConsumedValue;

    @Column(name = "covered_energy_kwh", precision = 19, scale = 4)
    private BigDecimal coveredEnergyKwh;

    @Column(name = "uncovered_energy_kwh", precision = 19, scale = 4)
    private BigDecimal uncoveredEnergyKwh;

    @Column(name = "benefit_amount", precision = 19, scale = 4)
    private BigDecimal benefitAmount;

    @Column(name = "regular_amount", precision = 19, scale = 4)
    private BigDecimal regularAmount;

    @Column(name = "gross_amount", precision = 19, scale = 4)
    private BigDecimal grossAmount;

    @Column(name = "net_amount", precision = 19, scale = 4)
    private BigDecimal netAmount;

    @Column(name = "quota_exhausted", nullable = false)
    private boolean quotaExhausted;

    @Column(name = "remaining_quota")
    private Integer remainingQuota;

    @Column(length = 512)
    private String note;

    @Column(name = "utilized_at", nullable = false)
    private OffsetDateTime utilizedAt;

    /**
     * Executes subscription utilization for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     */
    protected SubscriptionUtilization() {
        LOGGER.info(" Entering SubscriptionUtilization#SubscriptionUtilization");
        LOGGER.debug(" Entering SubscriptionUtilization#SubscriptionUtilization with debug context");
    }

    public SubscriptionUtilization(UUID id,
                                   SubscriptionAllocation allocation,
                                   SubscriptionPlan plan,
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
                                   Integer remainingQuota,
                                   String note,
                                   OffsetDateTime utilizedAt) {
        this.id = id;
        this.allocation = allocation;
        this.plan = plan;
        this.userId = userId;
        this.organizationId = organizationId;
        this.groupId = groupId;
        this.sessionReference = sessionReference;
        this.chargingCost = chargingCost;
        this.sessionFee = sessionFee;
        this.idleFee = idleFee;
        this.taxes = taxes;
        this.eligibleSubtotal = eligibleSubtotal;
        this.totalFeeDiscountAmount = totalFeeDiscountAmount;
        this.sessionFeeDiscountAmount = sessionFeeDiscountAmount;
        this.totalDiscountAmount = totalDiscountAmount;
        this.finalChargeExcludingTax = finalChargeExcludingTax;
        this.finalChargeIncludingTax = finalChargeIncludingTax;
        this.unitsConsumed = unitsConsumed;
        this.energyKwh = BigDecimal.valueOf(unitsConsumed);
        this.quotaUnit = plan.getQuotaUnit().name();
        this.quotaConsumedValue = BigDecimal.valueOf(unitsConsumed);
        this.coveredEnergyKwh = BigDecimal.valueOf(unitsConsumed);
        this.uncoveredEnergyKwh = BigDecimal.ZERO;
        this.benefitAmount = totalDiscountAmount;
        this.regularAmount = finalChargeIncludingTax;
        this.grossAmount = eligibleSubtotal.add(taxes);
        this.netAmount = finalChargeIncludingTax;
        this.quotaExhausted = remainingQuota != null && remainingQuota <= 0;
        this.remainingQuota = remainingQuota;
        this.note = note;
        this.utilizedAt = utilizedAt;
    }

    /**
     * Retrieves get id for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getId.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Retrieves get allocation for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getAllocation.
     */
    public SubscriptionAllocation getAllocation() {
        return allocation;
    }

    /**
     * Retrieves get plan for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getPlan.
     */
    public SubscriptionPlan getPlan() {
        return plan;
    }

    /**
     * Retrieves get user id for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getUserId.
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Retrieves get organization id for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getOrganizationId.
     */
    public UUID getOrganizationId() {
        return organizationId;
    }

    /**
     * Retrieves get group id for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getGroupId.
     */
    public UUID getGroupId() {
        return groupId;
    }

    /**
     * Retrieves get session reference for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getSessionReference.
     */
    public String getSessionReference() {
        return sessionReference;
    }

    /**
     * Retrieves get charging cost for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getChargingCost.
     */
    public BigDecimal getChargingCost() {
        return chargingCost;
    }

    /**
     * Retrieves get session fee for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getSessionFee.
     */
    public BigDecimal getSessionFee() {
        return sessionFee;
    }

    /**
     * Retrieves get idle fee for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getIdleFee.
     */
    public BigDecimal getIdleFee() {
        return idleFee;
    }

    /**
     * Retrieves get taxes for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getTaxes.
     */
    public BigDecimal getTaxes() {
        return taxes;
    }

    /**
     * Retrieves get eligible subtotal for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getEligibleSubtotal.
     */
    public BigDecimal getEligibleSubtotal() {
        return eligibleSubtotal;
    }

    /**
     * Retrieves get total fee discount amount for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getTotalFeeDiscountAmount.
     */
    public BigDecimal getTotalFeeDiscountAmount() {
        return totalFeeDiscountAmount;
    }

    /**
     * Retrieves get session fee discount amount for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getSessionFeeDiscountAmount.
     */
    public BigDecimal getSessionFeeDiscountAmount() {
        return sessionFeeDiscountAmount;
    }

    /**
     * Retrieves get total discount amount for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getTotalDiscountAmount.
     */
    public BigDecimal getTotalDiscountAmount() {
        return totalDiscountAmount;
    }

    /**
     * Retrieves get final charge excluding tax for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getFinalChargeExcludingTax.
     */
    public BigDecimal getFinalChargeExcludingTax() {
        return finalChargeExcludingTax;
    }

    /**
     * Retrieves get final charge including tax for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getFinalChargeIncludingTax.
     */
    public BigDecimal getFinalChargeIncludingTax() {
        return finalChargeIncludingTax;
    }

    /**
     * Retrieves get units consumed for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getUnitsConsumed.
     */
    public int getUnitsConsumed() {
        return unitsConsumed;
    }

    public BigDecimal getEnergyKwh() {
        return energyKwh != null ? energyKwh : BigDecimal.valueOf(unitsConsumed);
    }

    public String getQuotaUnit() {
        return quotaUnit == null ? QuotaUnit.SESSION.name() : quotaUnit;
    }

    public BigDecimal getQuotaConsumedValue() {
        return quotaConsumedValue != null ? quotaConsumedValue : BigDecimal.valueOf(unitsConsumed);
    }

    public BigDecimal getCoveredEnergyKwh() {
        return coveredEnergyKwh != null ? coveredEnergyKwh : BigDecimal.valueOf(unitsConsumed);
    }

    public BigDecimal getUncoveredEnergyKwh() {
        return uncoveredEnergyKwh != null ? uncoveredEnergyKwh : BigDecimal.ZERO;
    }

    public BigDecimal getBenefitAmount() {
        return benefitAmount != null ? benefitAmount : totalDiscountAmount;
    }

    public BigDecimal getRegularAmount() {
        return regularAmount != null ? regularAmount : finalChargeIncludingTax;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount != null ? grossAmount : eligibleSubtotal.add(taxes);
    }

    public BigDecimal getNetAmount() {
        return netAmount != null ? netAmount : finalChargeIncludingTax;
    }

    public boolean isQuotaExhausted() {
        return quotaExhausted || (remainingQuota != null && remainingQuota <= 0);
    }

    public void applyAdminMetrics(BigDecimal energyKwh,
                                  QuotaUnit quotaUnit,
                                  BigDecimal quotaConsumedValue,
                                  BigDecimal coveredEnergyKwh,
                                  BigDecimal uncoveredEnergyKwh,
                                  BigDecimal benefitAmount,
                                  BigDecimal regularAmount,
                                  BigDecimal grossAmount,
                                  BigDecimal netAmount,
                                  boolean quotaExhausted) {
        this.energyKwh = energyKwh;
        this.quotaUnit = quotaUnit == null ? QuotaUnit.SESSION.name() : quotaUnit.name();
        this.quotaConsumedValue = quotaConsumedValue;
        this.coveredEnergyKwh = coveredEnergyKwh;
        this.uncoveredEnergyKwh = uncoveredEnergyKwh;
        this.benefitAmount = benefitAmount;
        this.regularAmount = regularAmount;
        this.grossAmount = grossAmount;
        this.netAmount = netAmount;
        this.quotaExhausted = quotaExhausted;
    }

    /**
     * Retrieves get remaining quota for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getRemainingQuota.
     */
    public Integer getRemainingQuota() {
        return remainingQuota;
    }

    /**
     * Retrieves get note for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getNote.
     */
    public String getNote() {
        return note;
    }

    /**
     * Retrieves get utilized at for `SubscriptionUtilization`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.domain`.
     * @return result produced by getUtilizedAt.
     */
    public OffsetDateTime getUtilizedAt() {
        return utilizedAt;
    }
}
