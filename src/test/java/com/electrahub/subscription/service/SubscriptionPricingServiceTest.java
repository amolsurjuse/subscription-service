package com.electrahub.subscription.service;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.electrahub.subscription.api.dto.PreviewSubscriptionUtilizationRequest;
import com.electrahub.subscription.api.dto.RecordSubscriptionUtilizationRequest;
import com.electrahub.subscription.api.dto.SubscriptionUtilizationPreviewResponse;
import com.electrahub.subscription.api.dto.SubscriptionUtilizationResponse;
import com.electrahub.subscription.domain.AllocationStatus;
import com.electrahub.subscription.domain.AllocationType;
import com.electrahub.subscription.domain.AuditAction;
import com.electrahub.subscription.domain.DiscountType;
import com.electrahub.subscription.domain.SubscriptionAllocation;
import com.electrahub.subscription.domain.SubscriptionAuditLog;
import com.electrahub.subscription.domain.SubscriptionPlan;
import com.electrahub.subscription.domain.SubscriptionUtilization;
import com.electrahub.subscription.repository.SubscriptionAllocationRepository;
import com.electrahub.subscription.repository.SubscriptionAuditLogRepository;
import com.electrahub.subscription.repository.SubscriptionPlanRepository;
import com.electrahub.subscription.repository.SubscriptionUtilizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionPricingServiceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionPricingServiceTest.class);


    private InMemoryAllocationStore allocationStore;
    private InMemoryUtilizationStore utilizationStore;
    private InMemoryAuditStore auditStore;
    private SubscriptionPricingService subscriptionPricingService;

    /**
     * Updates set up for `SubscriptionPricingServiceTest`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     */
    @BeforeEach
    void setUp() {
        LOGGER.info(" Entering SubscriptionPricingServiceTest#setUp");
        LOGGER.debug(" Entering SubscriptionPricingServiceTest#setUp with debug context");
        allocationStore = new InMemoryAllocationStore();
        utilizationStore = new InMemoryUtilizationStore();
        auditStore = new InMemoryAuditStore();

        SubscriptionAllocationRepository allocationRepository = allocationStore.createRepository();
        SubscriptionPlanRepository planRepository = createPlanRepository();
        SubscriptionUtilizationRepository utilizationRepository = utilizationStore.createRepository();
        SubscriptionAuditService auditService = new SubscriptionAuditService(auditStore.createRepository());

        SubscriptionAllocationService subscriptionAllocationService = new SubscriptionAllocationService(
                allocationRepository,
                null,
                auditService
        ) {
            @Override
            public SubscriptionAllocation requireAllocation(UUID allocationId) {
                return allocationStore.findById(allocationId).orElseThrow();
            }
        };

        subscriptionPricingService = new SubscriptionPricingService(
                allocationRepository,
                utilizationRepository,
                subscriptionAllocationService,
                auditService
        );
    }

    private SubscriptionPlanRepository createPlanRepository() {
        return (SubscriptionPlanRepository) Proxy.newProxyInstance(
                SubscriptionPlanRepository.class.getClassLoader(),
                new Class[]{SubscriptionPlanRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByCodeIgnoreCase" -> Optional.empty();
                    case "toString" -> "InMemoryPlanRepository";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    /**
     * Executes preview applies total and session discounts without discounting taxes for `SubscriptionPricingServiceTest`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     */
    @Test
    void previewAppliesTotalAndSessionDiscountsWithoutDiscountingTaxes() {
        SubscriptionAllocation allocation = buildAllocation(AllocationType.USER, 10, 0);
        allocationStore.allocations = List.of(allocation);

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
    void kwhPlanDiscountsOnlyCoveredEnergyAndExcludesTimeIdleFeesAndTaxes() {
        OffsetDateTime now = OffsetDateTime.now();
        SubscriptionPlan plan = new SubscriptionPlan(
                UUID.randomUUID(), "NEW_USER_20_OFF_500KWH_1Y", "New driver energy benefit",
                "20% off covered charging energy", "USD", DiscountType.PERCENTAGE, new BigDecimal("20"),
                DiscountType.NONE, BigDecimal.ZERO, 500,
                com.electrahub.subscription.domain.PlanVisibility.PUBLIC,
                com.electrahub.subscription.domain.PlanCategory.DRIVER_PUBLIC,
                com.electrahub.subscription.domain.PricingModel.FREE,
                com.electrahub.subscription.domain.BenefitDisplayMode.DISCOUNT,
                com.electrahub.subscription.domain.QuotaUnit.KWH,
                new BigDecimal("500"), null, 365, null, "US", 1, false, "test", true, now
        );
        SubscriptionAllocation allocation = new SubscriptionAllocation(
                UUID.randomUUID(), plan, AllocationType.USER, UUID.randomUUID(), null, null, null,
                now.minusDays(1), now.plusDays(365), AllocationStatus.ACTIVE, "tester", now
        );
        allocationStore.allocations = List.of(allocation);

        SubscriptionUtilizationPreviewResponse response = subscriptionPricingService.preview(
                new PreviewSubscriptionUtilizationRequest(
                        allocation.getId(), allocation.getUserId(), null, null, "energy-only-session",
                        new BigDecimal("0.3410"), new BigDecimal("0.2000"), new BigDecimal("2.0000"),
                        new BigDecimal("0.2000"), 2, new BigDecimal("1.1000"), new BigDecimal("1.0800")
                )
        );

        assertThat(response.eligibleSubtotal()).isEqualByComparingTo("0.3410");
        assertThat(response.totalFeeDiscountAmount()).isEqualByComparingTo("0.0682");
        assertThat(response.sessionFeeDiscountAmount()).isZero();
        assertThat(response.benefitAmount()).isEqualByComparingTo("0.0670");
        assertThat(response.grossAmount()).isEqualByComparingTo("2.7410");
        assertThat(response.netAmount()).isEqualByComparingTo("2.6740");
        assertThat(response.coveredEnergyKwh()).isEqualByComparingTo("1.0800");
    }

    @Test
    void allFeesPercentageCoversEnergyTimeSessionAndIdleButExcludesTaxes() {
        OffsetDateTime now = OffsetDateTime.now();
        SubscriptionPlan plan = new SubscriptionPlan(
                UUID.randomUUID(), "FREE_CHARGING_USER_GRANT", "Free Charging - All Fees",
                "100% off all non-tax charging fees", "USD",
                DiscountType.ALL_FEES_PERCENTAGE, new BigDecimal("100"),
                DiscountType.NONE, BigDecimal.ZERO, 100,
                com.electrahub.subscription.domain.PlanVisibility.ADMIN_ONLY,
                com.electrahub.subscription.domain.PlanCategory.DRIVER_PUBLIC,
                com.electrahub.subscription.domain.PricingModel.FREE,
                com.electrahub.subscription.domain.BenefitDisplayMode.INCLUDED_QUOTA,
                com.electrahub.subscription.domain.QuotaUnit.KWH,
                new BigDecimal("100"), BigDecimal.ZERO, null, null, "US", 20, false, "test", true, now
        );
        SubscriptionAllocation allocation = new SubscriptionAllocation(
                UUID.randomUUID(), plan, AllocationType.USER, UUID.randomUUID(), null, null, 100,
                new BigDecimal("100"), now.minusMinutes(1), null, AllocationStatus.ACTIVE, "admin",
                com.electrahub.subscription.domain.AllocationSource.ADMIN_GRANTED,
                "Admin grant", "Customer care credit", null, null, null, now
        );
        allocationStore.allocations = List.of(allocation);

        SubscriptionUtilizationPreviewResponse response = subscriptionPricingService.preview(
                new PreviewSubscriptionUtilizationRequest(
                        allocation.getId(), allocation.getUserId(), null, null, "free-session",
                        new BigDecimal("12.00"), new BigDecimal("1.50"), new BigDecimal("2.00"),
                        new BigDecimal("0.75"), 10, new BigDecimal("10"), new BigDecimal("10")
                )
        );

        assertThat(response.eligibleSubtotal()).isEqualByComparingTo("15.5000");
        assertThat(response.totalFeeDiscountAmount()).isEqualByComparingTo("15.5000");
        assertThat(response.finalChargeExcludingTax()).isZero();
        assertThat(response.finalChargeIncludingTax()).isEqualByComparingTo("0.7500");
        assertThat(response.remainingQuotaValueAfterUse()).isEqualByComparingTo("90.0000");
    }

    /**
     * Executes record consumes quota and persists utilization for `SubscriptionPricingServiceTest`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     */
    @Test
    void recordConsumesQuotaAndPersistsUtilization() {
        SubscriptionAllocation allocation = buildAllocation(AllocationType.ORGANIZATION, 5, 1);
        allocationStore.allocations = List.of(allocation);

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
        assertThat(utilizationStore.savedUtilizations).hasSize(1);
        assertThat(auditStore.savedLogs).hasSize(1);
        assertThat(auditStore.savedLogs.getFirst().getAction()).isEqualTo(AuditAction.UTILIZATION_RECORDED);
    }

    /**
     * Executes preview caps session discount to remaining eligible amount for `SubscriptionPricingServiceTest`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     */
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
        allocationStore.allocations = List.of(allocation);

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

    /**
     * Executes preview prefers user allocation over broader organization allocation for `SubscriptionPricingServiceTest`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     */
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
        allocationStore.allocations = List.of(organizationAllocation, userAllocation);

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

    /**
     * Creates build allocation for `SubscriptionPricingServiceTest`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param allocationType input consumed by buildAllocation.
     * @param quotaLimit input consumed by buildAllocation.
     * @param consumedUnits input consumed by buildAllocation.
     * @return result produced by buildAllocation.
     */
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

    private static final class InMemoryAllocationStore {
        private List<SubscriptionAllocation> allocations = List.of();

        private SubscriptionAllocationRepository createRepository() {
            return (SubscriptionAllocationRepository) Proxy.newProxyInstance(
                    SubscriptionAllocationRepository.class.getClassLoader(),
                    new Class[]{SubscriptionAllocationRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "findAllWithPlan", "findActiveAllocationsForTarget" -> allocations;
                        case "findDetailedById" -> findById((UUID) args[0]);
                        case "toString" -> "InMemoryAllocationRepository";
                        default -> throw new UnsupportedOperationException(method.getName());
                    }
            );
        }

        private Optional<SubscriptionAllocation> findById(UUID id) {
            return allocations.stream()
                    .filter(allocation -> allocation.getId().equals(id))
                    .findFirst();
        }
    }

    private static final class InMemoryUtilizationStore {
        private final List<SubscriptionUtilization> savedUtilizations = new ArrayList<>();

        private SubscriptionUtilizationRepository createRepository() {
            return (SubscriptionUtilizationRepository) Proxy.newProxyInstance(
                    SubscriptionUtilizationRepository.class.getClassLoader(),
                    new Class[]{SubscriptionUtilizationRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "save" -> {
                            SubscriptionUtilization utilization = (SubscriptionUtilization) args[0];
                            savedUtilizations.add(utilization);
                            yield utilization;
                        }
                        case "findTop100ByUserIdOrderByUtilizedAtDesc" -> savedUtilizations.stream()
                                .filter(utilization -> utilization.getUserId().equals(args[0]))
                                .toList();
                        case "toString" -> "InMemoryUtilizationRepository";
                        default -> throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }

    private static final class InMemoryAuditStore {
        private final List<SubscriptionAuditLog> savedLogs = new ArrayList<>();

        private SubscriptionAuditLogRepository createRepository() {
            return (SubscriptionAuditLogRepository) Proxy.newProxyInstance(
                    SubscriptionAuditLogRepository.class.getClassLoader(),
                    new Class[]{SubscriptionAuditLogRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "save" -> {
                            SubscriptionAuditLog log = (SubscriptionAuditLog) args[0];
                            savedLogs.add(log);
                            yield log;
                        }
                        case "toString" -> "InMemoryAuditRepository";
                        default -> throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }
}
