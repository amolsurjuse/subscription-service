package com.electrahub.subscription.service;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.electrahub.subscription.api.dto.PagedResponse;
import com.electrahub.subscription.api.dto.SubscriptionAuditLogResponse;
import com.electrahub.subscription.domain.AuditAction;
import com.electrahub.subscription.repository.SubscriptionAuditLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
        LOGGER.info(" Entering SubscriptionAuditQueryService#SubscriptionAuditQueryService");
        LOGGER.debug(" Entering SubscriptionAuditQueryService#SubscriptionAuditQueryService with debug context");
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

    @Transactional(readOnly = true)
    public PagedResponse<SubscriptionAuditLogResponse> searchPaged(UUID planId,
                                                                   UUID allocationId,
                                                                   UUID userId,
                                                                   UUID organizationId,
                                                                   UUID groupId,
                                                                   AuditAction action,
                                                                   String entityType,
                                                                   String query,
                                                                   OffsetDateTime createdFrom,
                                                                   OffsetDateTime createdTo,
                                                                   int limit,
                                                                   int offset) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        int safeOffset = Math.max(0, offset);
        int page = safeOffset / safeLimit;

        List<AuditAction> filteredActions = resolveActionFilters(action, entityType);
        boolean actionsEmpty = filteredActions.isEmpty();
        List<AuditAction> actionParams = actionsEmpty ? Arrays.asList(AuditAction.values()) : filteredActions;
        String normalizedQuery = normalizeOptionalText(query);
        boolean queryEmpty = normalizedQuery == null;
        String queryPattern = queryEmpty ? "%" : "%" + normalizedQuery.toLowerCase(Locale.ROOT) + "%";
        boolean createdFromPresent = createdFrom != null;
        boolean createdToPresent = createdTo != null;

        var pageable = PageRequest.of(page, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt"));
        var pageResult = subscriptionAuditLogRepository.searchPaged(
                planId,
                allocationId,
                userId,
                organizationId,
                groupId,
                actionParams,
                actionsEmpty,
                queryEmpty,
                queryPattern,
                createdFromPresent,
                createdFrom,
                createdToPresent,
                createdTo,
                pageable
        );

        var items = pageResult.getContent().stream()
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
        long total = pageResult.getTotalElements();
        int totalPages = Math.max(pageResult.getTotalPages(), total > 0 ? 1 : 0);

        return new PagedResponse<>(
                items,
                total,
                safeLimit,
                safeOffset,
                page,
                totalPages,
                pageResult.hasNext(),
                pageResult.hasPrevious()
        );
    }

    private List<AuditAction> resolveActionFilters(AuditAction action, String entityType) {
        if (action != null) {
            return List.of(action);
        }

        if (entityType == null || entityType.isBlank()) {
            return List.of();
        }

        return switch (entityType.trim().toUpperCase()) {
            case "PLAN" -> List.of(AuditAction.PLAN_CREATED, AuditAction.PLAN_UPDATED);
            case "ALLOCATION" -> List.of(
                    AuditAction.ALLOCATION_CREATED,
                    AuditAction.ALLOCATION_GRANTED,
                    AuditAction.ALLOCATION_STATUS_CHANGED,
                    AuditAction.ALLOCATION_PAUSED,
                    AuditAction.ALLOCATION_REVOKED
            );
            case "UTILIZATION" -> List.of(
                    AuditAction.UTILIZATION_PREVIEWED,
                    AuditAction.UTILIZATION_RECORDED,
                    AuditAction.UTILIZATION_DUPLICATE_IGNORED
            );
            default -> List.of();
        };
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
