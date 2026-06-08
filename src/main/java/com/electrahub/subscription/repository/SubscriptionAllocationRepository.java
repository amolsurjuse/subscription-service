package com.electrahub.subscription.repository;

import com.electrahub.subscription.domain.SubscriptionAllocation;
import com.electrahub.subscription.domain.AllocationStatus;
import com.electrahub.subscription.domain.AllocationSource;
import com.electrahub.subscription.domain.AllocationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.time.OffsetDateTime;
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

    @Query(
            value = """
                    select a from SubscriptionAllocation a
                    join fetch a.plan p
                    where (:userId is null or a.userId = :userId)
                      and (:organizationId is null or a.organizationId = :organizationId)
                      and (:groupId is null or a.groupId = :groupId)
                      and (:planId is null or p.id = :planId)
                      and (:allocationType is null or a.allocationType = :allocationType)
                      and (:status is null or a.status = :status)
                      and (:source is null or a.source = :source)
                      and (:enterpriseId is null or a.enterpriseId = :enterpriseId or p.enterpriseId = :enterpriseId)
                      and (:exhausted is null or (
                          :exhausted = true and (
                              (a.quotaLimitValue is not null and a.consumedValue >= a.quotaLimitValue)
                              or (a.quotaLimitValue is null and p.defaultQuotaValue is not null and a.consumedValue >= p.defaultQuotaValue)
                              or (a.quotaLimit is not null and a.consumedUnits >= a.quotaLimit)
                              or (a.quotaLimit is null and p.defaultQuotaLimit is not null and a.consumedUnits >= p.defaultQuotaLimit)
                          )
                          or :exhausted = false and not (
                              (a.quotaLimitValue is not null and a.consumedValue >= a.quotaLimitValue)
                              or (a.quotaLimitValue is null and p.defaultQuotaValue is not null and a.consumedValue >= p.defaultQuotaValue)
                              or (a.quotaLimit is not null and a.consumedUnits >= a.quotaLimit)
                              or (a.quotaLimit is null and p.defaultQuotaLimit is not null and a.consumedUnits >= p.defaultQuotaLimit)
                          )
                      ))
                      and (:activeOnly = false or (
                          a.status = com.electrahub.subscription.domain.AllocationStatus.ACTIVE
                          and a.startsAt <= :now
                          and (a.endsAt is null or a.endsAt >= :now)
                          and p.active = true
                      ))
                    """,
            countQuery = """
                    select count(a) from SubscriptionAllocation a
                    join a.plan p
                    where (:userId is null or a.userId = :userId)
                      and (:organizationId is null or a.organizationId = :organizationId)
                      and (:groupId is null or a.groupId = :groupId)
                      and (:planId is null or p.id = :planId)
                      and (:allocationType is null or a.allocationType = :allocationType)
                      and (:status is null or a.status = :status)
                      and (:source is null or a.source = :source)
                      and (:enterpriseId is null or a.enterpriseId = :enterpriseId or p.enterpriseId = :enterpriseId)
                      and (:exhausted is null or (
                          :exhausted = true and (
                              (a.quotaLimitValue is not null and a.consumedValue >= a.quotaLimitValue)
                              or (a.quotaLimitValue is null and p.defaultQuotaValue is not null and a.consumedValue >= p.defaultQuotaValue)
                              or (a.quotaLimit is not null and a.consumedUnits >= a.quotaLimit)
                              or (a.quotaLimit is null and p.defaultQuotaLimit is not null and a.consumedUnits >= p.defaultQuotaLimit)
                          )
                          or :exhausted = false and not (
                              (a.quotaLimitValue is not null and a.consumedValue >= a.quotaLimitValue)
                              or (a.quotaLimitValue is null and p.defaultQuotaValue is not null and a.consumedValue >= p.defaultQuotaValue)
                              or (a.quotaLimit is not null and a.consumedUnits >= a.quotaLimit)
                              or (a.quotaLimit is null and p.defaultQuotaLimit is not null and a.consumedUnits >= p.defaultQuotaLimit)
                          )
                      ))
                      and (:activeOnly = false or (
                          a.status = com.electrahub.subscription.domain.AllocationStatus.ACTIVE
                          and a.startsAt <= :now
                          and (a.endsAt is null or a.endsAt >= :now)
                          and p.active = true
                      ))
                    """
    )
    Page<SubscriptionAllocation> searchPaged(UUID userId,
                                             UUID organizationId,
                                             UUID groupId,
                                             UUID planId,
                                             AllocationType allocationType,
                                             AllocationStatus status,
                                             AllocationSource source,
                                             UUID enterpriseId,
                                             Boolean exhausted,
                                             boolean activeOnly,
                                             OffsetDateTime now,
                                             Pageable pageable);
}
