package com.electrahub.subscription.domain;

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

    @Column(name = "remaining_quota")
    private Integer remainingQuota;

    @Column(length = 512)
    private String note;

    @Column(name = "utilized_at", nullable = false)
    private OffsetDateTime utilizedAt;

    protected SubscriptionUtilization() {
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
        this.remainingQuota = remainingQuota;
        this.note = note;
        this.utilizedAt = utilizedAt;
    }

    public UUID getId() {
        return id;
    }

    public SubscriptionAllocation getAllocation() {
        return allocation;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public String getSessionReference() {
        return sessionReference;
    }

    public BigDecimal getChargingCost() {
        return chargingCost;
    }

    public BigDecimal getSessionFee() {
        return sessionFee;
    }

    public BigDecimal getIdleFee() {
        return idleFee;
    }

    public BigDecimal getTaxes() {
        return taxes;
    }

    public BigDecimal getEligibleSubtotal() {
        return eligibleSubtotal;
    }

    public BigDecimal getTotalFeeDiscountAmount() {
        return totalFeeDiscountAmount;
    }

    public BigDecimal getSessionFeeDiscountAmount() {
        return sessionFeeDiscountAmount;
    }

    public BigDecimal getTotalDiscountAmount() {
        return totalDiscountAmount;
    }

    public BigDecimal getFinalChargeExcludingTax() {
        return finalChargeExcludingTax;
    }

    public BigDecimal getFinalChargeIncludingTax() {
        return finalChargeIncludingTax;
    }

    public int getUnitsConsumed() {
        return unitsConsumed;
    }

    public Integer getRemainingQuota() {
        return remainingQuota;
    }

    public String getNote() {
        return note;
    }

    public OffsetDateTime getUtilizedAt() {
        return utilizedAt;
    }
}
