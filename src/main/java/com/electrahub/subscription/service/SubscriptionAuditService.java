package com.electrahub.subscription.service;

import com.electrahub.subscription.domain.AuditAction;
import com.electrahub.subscription.domain.SubscriptionAuditLog;
import com.electrahub.subscription.repository.SubscriptionAuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class SubscriptionAuditService {

    private final SubscriptionAuditLogRepository subscriptionAuditLogRepository;

    public SubscriptionAuditService(SubscriptionAuditLogRepository subscriptionAuditLogRepository) {
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

    private String normalizeActor(String actor) {
        String normalized = actor == null ? "" : actor.trim();
        return normalized.isBlank() ? "system" : normalized;
    }
}
