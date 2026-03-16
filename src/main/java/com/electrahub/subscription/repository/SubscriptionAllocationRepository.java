package com.electrahub.subscription.repository;

import com.electrahub.subscription.domain.SubscriptionAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionAllocationRepository extends JpaRepository<SubscriptionAllocation, UUID> {

    @Query("select a from SubscriptionAllocation a join fetch a.plan")
    List<SubscriptionAllocation> findAllWithPlan();

    @Query("select a from SubscriptionAllocation a join fetch a.plan where a.id = :allocationId")
    Optional<SubscriptionAllocation> findDetailedById(UUID allocationId);
}
