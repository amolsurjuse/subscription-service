package com.electrahub.subscription.service;

import com.electrahub.subscription.api.dto.PreviewSubscriptionUtilizationRequest;
import com.electrahub.subscription.api.dto.RecordSubscriptionUtilizationRequest;
import com.electrahub.subscription.api.dto.SubscriptionUtilizationPreviewResponse;
import com.electrahub.subscription.api.dto.SubscriptionUtilizationResponse;
import com.electrahub.subscription.domain.AllocationStatus;
import com.electrahub.subscription.domain.AllocationType;
import com.electrahub.subscription.domain.DiscountType;
import com.electrahub.subscription.domain.SubscriptionAllocation;
import com.electrahub.subscription.domain.SubscriptionPlan;
import com.electrahub.subscription.repository.SubscriptionAllocationRepository;
import com.electrahub.subscription.repository.SubscriptionUtilizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionPricingServiceTest {

    @Mock
    private SubscriptionAllocationRepository subscriptionAllocationRepository;

    @Mock
    private SubscriptionUtilizationRepository subscriptionUtilizationRepository;

    @Mock
    private SubscriptionAuditService subscriptionAuditService;

    private SubscriptionPricingService subscriptionPricingService;

    @BeforeEach
    void setUp() {
        SubscriptionAllocationService subscriptionAllocationService = new SubscriptionAllocationService(
                subscriptionAllocationRepository,
                null,
                subscriptionAuditService
        ) {
            @Override
            public SubscriptionAllocation requireAllocation(UUID allocationId) {
                return subscriptionAllocationRepository.findDetailedById(allocationId).orElseThrow();
            }
        };

        subscriptionPricingService = new SubscriptionPricingService(
                subscriptionAllocationRepository,
                subscriptionUtilizationRepository,
                subscriptionAllocationService,
                subscriptionAuditService
        );
    }

    @Test
    void previewAppliesTotalAndSessionDiscountsWithoutDiscountingTaxes() {
        SubscriptionAllocation allocation = buildAllocation(AllocationType.USER, 10, 0);
        when(subscriptionAllocationRepository.findAllWithPlan()).thenReturn(List.of(allocation));

        SubscriptionUtilizationPreviewResponse response = subscriptionPricingService.preview(
                new PreviewSubscriptionUtilizationRequest(
                        null,
                        allocation.getUserId(),
                        null,
                        null,
                        "sess-1",
                        new BigDecimal("20.00"),
                        new BigDecimal("5.00"),
                        new BigDecimal("3.00"),
                        new BigDecimal("2.00"),
                        2
                )
        );

        assertThat(response.eligibleSubtotal()).isEqualByComparingTo("28.0000");
        assertThat(response.totalFeeDiscountAmount()).isEqualByComparingTo("2.8000");
        assertThat(response.sessionFeeDiscountAmount()).isEqualByComparingTo("1.0000");
        assertThat(response.totalDiscountAmount()).isEqualByComparingTo("3.8000");
        assertThat(response.finalChargeExcludingTax()).isEqualByComparingTo("24.2000");
        assertThat(response.finalChargeIncludingTax()).isEqualByComparingTo("26.2000");
        assertThat(response.remainingQuotaAfterUse()).isEqualTo(8);
    }

    @Test
    void recordConsumesQuotaAndPersistsUtilization() {
        SubscriptionAllocation allocation = buildAllocation(AllocationType.ORGANIZATION, 5, 1);
        when(subscriptionAllocationRepository.findAllWithPlan()).thenReturn(List.of(allocation));
        when(subscriptionUtilizationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SubscriptionUtilizationResponse response = subscriptionPricingService.record(
                new RecordSubscriptionUtilizationRequest(
                        null,
                        UUID.randomUUID(),
                        allocation.getOrganizationId(),
                        null,
                        "sess-2",
                        new BigDecimal("10.00"),
                        new BigDecimal("2.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("1.50"),
                        2,
                        "charge completed",
                        "billing-engine"
                )
        );

        assertThat(response.totalDiscountAmount()).isEqualByComparingTo("2.2000");
        assertThat(response.remainingQuota()).isEqualTo(2);
        assertThat(allocation.getConsumedUnits()).isEqualTo(3);
        verify(subscriptionUtilizationRepository).save(any());
        verify(subscriptionAuditService).record(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void previewCapsSessionDiscountToRemainingEligibleAmount() {
        OffsetDateTime now = OffsetDateTime.now();
        SubscriptionPlan plan = new SubscriptionPlan(
                UUID.randomUUID(),
                "STACKED",
                "Stacked Saver",
                "Large total fee discount with session top-up",
                "USD",
                DiscountType.PERCENTAGE,
                new BigDecimal("90"),
                DiscountType.FIXED_AMOUNT,
                new BigDecimal("5"),
                null,
                true,
                now
        );
        SubscriptionAllocation allocation = new SubscriptionAllocation(
                UUID.randomUUID(),
                plan,
                AllocationType.USER,
                UUID.randomUUID(),
                null,
                null,
                null,
                now.minusDays(1),
                now.plusDays(10),
                AllocationStatus.ACTIVE,
                "tester",
                now
        );
        when(subscriptionAllocationRepository.findAllWithPlan()).thenReturn(List.of(allocation));

        SubscriptionUtilizationPreviewResponse response = subscriptionPricingService.preview(
                new PreviewSubscriptionUtilizationRequest(
                        null,
                        allocation.getUserId(),
                        null,
                        null,
                        "sess-cap",
                        BigDecimal.ZERO,
                        new BigDecimal("10.00"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        1
                )
        );

        assertThat(response.totalFeeDiscountAmount()).isEqualByComparingTo("9.0000");
        assertThat(response.sessionFeeDiscountAmount()).isEqualByComparingTo("1.0000");
        assertThat(response.totalDiscountAmount()).isEqualByComparingTo("10.0000");
        assertThat(response.finalChargeExcludingTax()).isEqualByComparingTo("0.0000");
    }

    @Test
    void previewPrefersUserAllocationOverBroaderOrganizationAllocation() {
        OffsetDateTime now = OffsetDateTime.now();
        UUID userId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();

        SubscriptionAllocation organizationAllocation = new SubscriptionAllocation(
                UUID.randomUUID(),
                new SubscriptionPlan(
                        UUID.randomUUID(),
                        "ORG10",
                        "Org 10",
                        "Organization subscription",
                        "USD",
                        DiscountType.PERCENTAGE,
                        new BigDecimal("10"),
                        DiscountType.NONE,
                        BigDecimal.ZERO,
                        null,
                        true,
                        now
                ),
                AllocationType.ORGANIZATION,
                null,
                organizationId,
                null,
                null,
                now.minusDays(1),
                now.plusDays(10),
                AllocationStatus.ACTIVE,
                "tester",
                now
        );
        SubscriptionAllocation userAllocation = new SubscriptionAllocation(
                UUID.randomUUID(),
                new SubscriptionPlan(
                        UUID.randomUUID(),
                        "USER25",
                        "User 25",
                        "User specific subscription",
                        "USD",
                        DiscountType.PERCENTAGE,
                        new BigDecimal("25"),
                        DiscountType.NONE,
                        BigDecimal.ZERO,
                        null,
                        true,
                        now
                ),
                AllocationType.USER,
                userId,
                null,
                null,
                null,
                now.minusDays(1),
                now.plusDays(10),
                AllocationStatus.ACTIVE,
                "tester",
                now
        );
        when(subscriptionAllocationRepository.findAllWithPlan()).thenReturn(List.of(organizationAllocation, userAllocation));

        SubscriptionUtilizationPreviewResponse response = subscriptionPricingService.preview(
                new PreviewSubscriptionUtilizationRequest(
                        null,
                        userId,
                        organizationId,
                        null,
                        "sess-priority",
                        new BigDecimal("20.00"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        1
                )
        );

        assertThat(response.planCode()).isEqualTo("USER25");
        assertThat(response.totalFeeDiscountAmount()).isEqualByComparingTo("5.0000");
    }

    private SubscriptionAllocation buildAllocation(AllocationType allocationType, int quotaLimit, int consumedUnits) {
        OffsetDateTime now = OffsetDateTime.now();
        SubscriptionPlan plan = new SubscriptionPlan(
                UUID.randomUUID(),
                "TOTAL_SAVER",
                "Total Saver",
                "Discounts total and session fees",
                "USD",
                DiscountType.PERCENTAGE,
                new BigDecimal("10"),
                DiscountType.FIXED_AMOUNT,
                new BigDecimal("1"),
                quotaLimit,
                true,
                now
        );

        SubscriptionAllocation allocation = new SubscriptionAllocation(
                UUID.randomUUID(),
                plan,
                allocationType,
                allocationType == AllocationType.USER ? UUID.randomUUID() : null,
                allocationType != AllocationType.USER ? UUID.randomUUID() : null,
                allocationType == AllocationType.ORGANIZATION_GROUP ? UUID.randomUUID() : null,
                null,
                now.minusDays(1),
                now.plusDays(30),
                AllocationStatus.ACTIVE,
                "tester",
                now
        );
        allocation.incrementConsumedUnits(consumedUnits);
        return allocation;
    }
}
