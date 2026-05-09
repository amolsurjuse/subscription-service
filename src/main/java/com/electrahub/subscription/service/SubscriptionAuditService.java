package com.electrahub.subscription.service;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.electrahub.subscription.domain.AuditAction;
import com.electrahub.subscription.domain.SubscriptionAuditLog;
import com.electrahub.subscription.repository.SubscriptionAuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class SubscriptionAuditService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionAuditService.class);


    private final SubscriptionAuditLogRepository subscriptionAuditLogRepository;

    /**
     * Executes subscription audit service for `SubscriptionAuditService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param subscriptionAuditLogRepository input consumed by SubscriptionAuditService.
     */
    public SubscriptionAuditService(SubscriptionAuditLogRepository subscriptionAuditLogRepository) {
        LOGGER.info(" Entering SubscriptionAuditService#SubscriptionAuditService");
        LOGGER.debug(" Entering SubscriptionAuditService#SubscriptionAuditService with debug context");
        this.subscriptionAuditLogRepository = subscriptionAuditLogRepository;
    }

    public void record(UUID planId,
                       UUID allocationId,
                       UUID userId,
                       UUID organizationId,
                       UUID groupId,
                       AuditAction action,
                       String actor,
                       String detail) {
        subscriptionAuditLogRepository.save(new SubscriptionAuditLog(
                UUID.randomUUID(),
                planId,
                allocationId,
                userId,
                organizationId,
                groupId,
                action,
                normalizeActor(actor),
                detail,
                OffsetDateTime.now()
        ));
    }

    /**
     * Executes normalize actor for `SubscriptionAuditService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param actor input consumed by normalizeActor.
     * @return result produced by normalizeActor.
     */
    private String normalizeActor(String actor) {
        String normalized = actor == null ? "" : actor.trim();
        return normalized.isBlank() ? "system" : normalized;
    }
}
