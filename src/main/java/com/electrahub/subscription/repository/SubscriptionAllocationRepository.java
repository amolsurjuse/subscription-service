package com.electrahub.subscription.repository;

import com.electrahub.subscription.domain.SubscriptionAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionAllocationRepository extends JpaRepository<SubscriptionAllocation, UUID> {

    /**
     * Retrieves find all with plan for `SubscriptionAllocationRepository`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.repository`.
     * @return result produced by findAllWithPlan.
     */
    @Query("select a from SubscriptionAllocation a join fetch a.plan")
    List<SubscriptionAllocation> findAllWithPlan();

    /**
     * Retrieves find detailed by id for `SubscriptionAllocationRepository`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.repository`.
     * @param allocationId input consumed by findDetailedById.
     * @return result produced by findDetailedById.
     */
    @Query("select a from SubscriptionAllocation a join fetch a.plan where a.id = :allocationId")
    Optional<SubscriptionAllocation> findDetailedById(UUID allocationId);
}
