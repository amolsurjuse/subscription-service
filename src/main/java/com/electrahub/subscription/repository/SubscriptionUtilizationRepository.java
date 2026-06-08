package com.electrahub.subscription.repository;

import com.electrahub.subscription.domain.SubscriptionUtilization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface SubscriptionUtilizationRepository extends JpaRepository<SubscriptionUtilization, UUID> {

    /**
     * Retrieves find top100 by user id order by utilized at desc for `SubscriptionUtilizationRepository`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.repository`.
     * @param userId input consumed by findTop100ByUserIdOrderByUtilizedAtDesc.
     * @return result produced by findTop100ByUserIdOrderByUtilizedAtDesc.
     */
    List<SubscriptionUtilization> findTop100ByUserIdOrderByUtilizedAtDesc(UUID userId);

    @Query(
            value = """
                    select u from SubscriptionUtilization u
                    join fetch u.plan p
                    join fetch u.allocation a
                    where (:userId is null or u.userId = :userId)
                      and (:allocationId is null or a.id = :allocationId)
                      and (:planId is null or p.id = :planId)
                      and (:enterpriseId is null or a.enterpriseId = :enterpriseId or p.enterpriseId = :enterpriseId)
                      and (:sessionReferenceEmpty = true or lower(coalesce(u.sessionReference, '')) like :sessionReferencePattern)
                      and (:fromPresent = false or u.utilizedAt >= :from)
                      and (:toPresent = false or u.utilizedAt <= :to)
                      and (:quotaExhausted is null or u.quotaExhausted = :quotaExhausted)
                    """,
            countQuery = """
                    select count(u) from SubscriptionUtilization u
                    join u.plan p
                    join u.allocation a
                    where (:userId is null or u.userId = :userId)
                      and (:allocationId is null or a.id = :allocationId)
                      and (:planId is null or p.id = :planId)
                      and (:enterpriseId is null or a.enterpriseId = :enterpriseId or p.enterpriseId = :enterpriseId)
                      and (:sessionReferenceEmpty = true or lower(coalesce(u.sessionReference, '')) like :sessionReferencePattern)
                      and (:fromPresent = false or u.utilizedAt >= :from)
                      and (:toPresent = false or u.utilizedAt <= :to)
                      and (:quotaExhausted is null or u.quotaExhausted = :quotaExhausted)
                    """
    )
    Page<SubscriptionUtilization> searchPaged(UUID userId,
                                             UUID allocationId,
                                             UUID planId,
                                             UUID enterpriseId,
                                             boolean sessionReferenceEmpty,
                                             String sessionReferencePattern,
                                             boolean fromPresent,
                                             OffsetDateTime from,
                                             boolean toPresent,
                                             OffsetDateTime to,
                                             Boolean quotaExhausted,
                                             Pageable pageable);
}
