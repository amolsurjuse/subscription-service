package com.electrahub.subscription.repository;

import com.electrahub.subscription.domain.SubscriptionPlan;
import com.electrahub.subscription.domain.BenefitDisplayMode;
import com.electrahub.subscription.domain.PlanCategory;
import com.electrahub.subscription.domain.PlanVisibility;
import com.electrahub.subscription.domain.PricingModel;
import com.electrahub.subscription.domain.QuotaUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    /**
     * Executes exists by code ignore case for `SubscriptionPlanRepository`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.repository`.
     * @param code input consumed by existsByCodeIgnoreCase.
     * @return result produced by existsByCodeIgnoreCase.
     */
    boolean existsByCodeIgnoreCase(String code);

    Optional<SubscriptionPlan> findByCodeIgnoreCase(String code);

    /**
     * Retrieves find all by order by updated at desc for `SubscriptionPlanRepository`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.repository`.
     * @param pageable input consumed by findAllByOrderByUpdatedAtDesc.
     * @return result produced by findAllByOrderByUpdatedAtDesc.
     */
    Page<SubscriptionPlan> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    @Query("""
            select p from SubscriptionPlan p
            where (:queryEmpty = true or lower(p.code) like :queryPattern or lower(p.name) like :queryPattern or lower(coalesce(p.description, '')) like :queryPattern)
              and (:visibility is null or p.visibility = :visibility)
              and (:planCategory is null or p.planCategory = :planCategory)
              and (:pricingModel is null or p.pricingModel = :pricingModel)
              and (:benefitDisplayMode is null or p.benefitDisplayMode = :benefitDisplayMode)
              and (:quotaUnit is null or p.quotaUnit = :quotaUnit)
              and (:enterpriseId is null or p.enterpriseId = :enterpriseId)
              and (:countryCodeEmpty = true or p.countryCode = :countryCode)
              and (:active is null or p.active = :active)
            """)
    Page<SubscriptionPlan> searchPaged(boolean queryEmpty,
                                       String queryPattern,
                                       PlanVisibility visibility,
                                       PlanCategory planCategory,
                                       PricingModel pricingModel,
                                       BenefitDisplayMode benefitDisplayMode,
                                       QuotaUnit quotaUnit,
                                       UUID enterpriseId,
                                       boolean countryCodeEmpty,
                                       String countryCode,
                                       Boolean active,
                                       Pageable pageable);
}
