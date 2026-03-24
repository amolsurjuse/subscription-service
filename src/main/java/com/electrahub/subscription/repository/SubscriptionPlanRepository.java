package com.electrahub.subscription.repository;

import com.electrahub.subscription.domain.SubscriptionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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

    /**
     * Retrieves find all by order by updated at desc for `SubscriptionPlanRepository`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.repository`.
     * @param pageable input consumed by findAllByOrderByUpdatedAtDesc.
     * @return result produced by findAllByOrderByUpdatedAtDesc.
     */
    Page<SubscriptionPlan> findAllByOrderByUpdatedAtDesc(Pageable pageable);
}
