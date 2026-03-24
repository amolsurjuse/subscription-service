package com.electrahub.subscription.service;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.electrahub.subscription.api.dto.SubscriptionAuditLogResponse;
import com.electrahub.subscription.repository.SubscriptionAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionAuditQueryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionAuditQueryService.class);


    private final SubscriptionAuditLogRepository subscriptionAuditLogRepository;

    /**
     * Executes subscription audit query service for `SubscriptionAuditQueryService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param subscriptionAuditLogRepository input consumed by SubscriptionAuditQueryService.
     */
    public SubscriptionAuditQueryService(SubscriptionAuditLogRepository subscriptionAuditLogRepository) {
        LOGGER.info("CODEx_ENTRY_LOG: Entering SubscriptionAuditQueryService#SubscriptionAuditQueryService");
        LOGGER.debug("CODEx_ENTRY_LOG: Entering SubscriptionAuditQueryService#SubscriptionAuditQueryService with debug context");
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
