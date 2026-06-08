package com.electrahub.subscription.repository;

import com.electrahub.subscription.domain.SubscriptionAuditLog;
import com.electrahub.subscription.domain.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface SubscriptionAuditLogRepository extends JpaRepository<SubscriptionAuditLog, UUID> {

    @Query("""
            select l from SubscriptionAuditLog l
            where (:planId is null or l.planId = :planId)
              and (:allocationId is null or l.allocationId = :allocationId)
              and (:userId is null or l.userId = :userId)
              and (:organizationId is null or l.organizationId = :organizationId)
              and (:groupId is null or l.groupId = :groupId)
            order by l.createdAt desc
            """)
    List<SubscriptionAuditLog> search(UUID planId,
                                      UUID allocationId,
                                      UUID userId,
                                      UUID organizationId,
                                      UUID groupId);

    @Query("""
            select l from SubscriptionAuditLog l
            where (:planId is null or l.planId = :planId)
              and (:allocationId is null or l.allocationId = :allocationId)
              and (:userId is null or l.userId = :userId)
              and (:organizationId is null or l.organizationId = :organizationId)
              and (:groupId is null or l.groupId = :groupId)
              and (:actionsEmpty = true or l.action in :actions)
              and (:queryEmpty = true or lower(l.actor) like :queryPattern)
              and (:createdFromPresent = false or l.createdAt >= :createdFrom)
              and (:createdToPresent = false or l.createdAt <= :createdTo)
            """)
    Page<SubscriptionAuditLog> searchPaged(UUID planId,
                                           UUID allocationId,
                                           UUID userId,
                                           UUID organizationId,
                                           UUID groupId,
                                           List<AuditAction> actions,
                                           boolean actionsEmpty,
                                           boolean queryEmpty,
                                           String queryPattern,
                                           boolean createdFromPresent,
                                           OffsetDateTime createdFrom,
                                           boolean createdToPresent,
                                           OffsetDateTime createdTo,
                                           Pageable pageable);
}
