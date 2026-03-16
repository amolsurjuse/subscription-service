package com.electrahub.subscription.service;

import com.electrahub.subscription.api.dto.SubscriptionAuditLogResponse;
import com.electrahub.subscription.repository.SubscriptionAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionAuditQueryService {

    private final SubscriptionAuditLogRepository subscriptionAuditLogRepository;

    public SubscriptionAuditQueryService(SubscriptionAuditLogRepository subscriptionAuditLogRepository) {
        this.subscriptionAuditLogRepository = subscriptionAuditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<SubscriptionAuditLogResponse> search(UUID planId,
                                                     UUID allocationId,
                                                     UUID userId,
                                                     UUID organizationId,
                                                     UUID groupId) {
        if (planId == null && allocationId == null && userId == null && organizationId == null && groupId == null) {
            throw new IllegalArgumentException("At least one audit log filter must be provided");
        }

        return subscriptionAuditLogRepository.search(planId, allocationId, userId, organizationId, groupId).stream()
                .limit(100)
                .map(log -> new SubscriptionAuditLogResponse(
                        log.getId(),
                        log.getPlanId(),
                        log.getAllocationId(),
                        log.getUserId(),
                        log.getOrganizationId(),
                        log.getGroupId(),
                        log.getAction(),
                        log.getActor(),
                        log.getDetail(),
                        log.getCreatedAt()
                ))
                .toList();
    }
}
