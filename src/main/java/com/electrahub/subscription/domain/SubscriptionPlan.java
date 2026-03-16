package com.electrahub.subscription.domain;

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

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected SubscriptionPlan() {
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
        this.active = active;
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public DiscountType getTotalFeeDiscountType() {
        return totalFeeDiscountType;
    }

    public BigDecimal getTotalFeeDiscountValue() {
        return totalFeeDiscountValue;
    }

    public DiscountType getSessionFeeDiscountType() {
        return sessionFeeDiscountType;
    }

    public BigDecimal getSessionFeeDiscountValue() {
        return sessionFeeDiscountValue;
    }

    public Integer getDefaultQuotaLimit() {
        return defaultQuotaLimit;
    }

    public boolean isActive() {
        return active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
