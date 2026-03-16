package com.electrahub.subscription.repository;

import com.electrahub.subscription.domain.SubscriptionAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
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
}
