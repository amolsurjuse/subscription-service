package com.electrahub.subscription.service;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.electrahub.subscription.api.dto.CreateSubscriptionAllocationRequest;
import com.electrahub.subscription.api.dto.SubscriptionAllocationResponse;
import com.electrahub.subscription.api.dto.UpdateAllocationStatusRequest;
import com.electrahub.subscription.api.error.NotFoundException;
import com.electrahub.subscription.domain.AllocationStatus;
import com.electrahub.subscription.domain.AllocationType;
import com.electrahub.subscription.domain.AuditAction;
import com.electrahub.subscription.domain.SubscriptionAllocation;
import com.electrahub.subscription.domain.SubscriptionPlan;
import com.electrahub.subscription.repository.SubscriptionAllocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionAllocationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionAllocationService.class);


    private final SubscriptionAllocationRepository subscriptionAllocationRepository;
    private final SubscriptionPlanService subscriptionPlanService;
    private final SubscriptionAuditService subscriptionAuditService;

    public SubscriptionAllocationService(SubscriptionAllocationRepository subscriptionAllocationRepository,
                                         SubscriptionPlanService subscriptionPlanService,
                                         SubscriptionAuditService subscriptionAuditService) {
        this.subscriptionAllocationRepository = subscriptionAllocationRepository;
        this.subscriptionPlanService = subscriptionPlanService;
        this.subscriptionAuditService = subscriptionAuditService;
    }

    /**
     * Creates create for `SubscriptionAllocationService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param request input consumed by create.
     * @return result produced by create.
     */
    @Transactional
    public SubscriptionAllocationResponse create(CreateSubscriptionAllocationRequest request) {
        LOGGER.info(" Entering SubscriptionAllocationService#create");
        LOGGER.debug(" Entering SubscriptionAllocationService#create with debug context");
        validateTarget(request);
        validateSchedule(request.startsAt(), request.endsAt());

        SubscriptionPlan plan = subscriptionPlanService.requirePlan(request.planId());
        AllocationStatus status = request.status() == null ? AllocationStatus.ACTIVE : request.status();
        OffsetDateTime now = OffsetDateTime.now();

        SubscriptionAllocation allocation = new SubscriptionAllocation(
                UUID.randomUUID(),
                plan,
                request.allocationType(),
                request.userId(),
                request.organizationId(),
                request.groupId(),
                request.quotaLimit(),
                request.startsAt(),
                request.endsAt(),
                status,
                normalizeActor(request.createdBy()),
                now
        );
        subscriptionAllocationRepository.save(allocation);

        subscriptionAuditService.record(
                plan.getId(),
                allocation.getId(),
                allocation.getUserId(),
                allocation.getOrganizationId(),
                allocation.getGroupId(),
                AuditAction.ALLOCATION_CREATED,
                allocation.getCreatedBy(),
                "Created subscription allocation " + allocation.getId()
        );

        return toResponse(allocation);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionAllocationResponse> list(UUID userId,
                                                     UUID organizationId,
                                                     UUID groupId,
                                                     Boolean activeOnly) {
        OffsetDateTime now = OffsetDateTime.now();
        return subscriptionAllocationRepository.findAllWithPlan().stream()
                .filter(allocation -> userId == null || userId.equals(allocation.getUserId()))
                .filter(allocation -> organizationId == null || organizationId.equals(allocation.getOrganizationId()))
                .filter(allocation -> groupId == null || groupId.equals(allocation.getGroupId()))
                .filter(allocation -> !Boolean.TRUE.equals(activeOnly) || allocation.isActiveAt(now))
                .sorted(Comparator.comparing(SubscriptionAllocation::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    /**
     * Updates update status for `SubscriptionAllocationService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param allocationId input consumed by updateStatus.
     * @param request input consumed by updateStatus.
     * @return result produced by updateStatus.
     */
    @Transactional
    public SubscriptionAllocationResponse updateStatus(UUID allocationId, UpdateAllocationStatusRequest request) {
        SubscriptionAllocation allocation = requireAllocation(allocationId);
        allocation.setStatus(request.status());

        subscriptionAuditService.record(
                allocation.getPlan().getId(),
                allocation.getId(),
                allocation.getUserId(),
                allocation.getOrganizationId(),
                allocation.getGroupId(),
                AuditAction.ALLOCATION_STATUS_CHANGED,
                normalizeActor(request.actor()),
                buildStatusDetail(request)
        );

        return toResponse(allocation);
    }

    /**
     * Executes require allocation for `SubscriptionAllocationService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param allocationId input consumed by requireAllocation.
     * @return result produced by requireAllocation.
     */
    @Transactional(readOnly = true)
    public SubscriptionAllocation requireAllocation(UUID allocationId) {
        return subscriptionAllocationRepository.findDetailedById(allocationId)
                .orElseThrow(() -> new NotFoundException("Subscription allocation not found: " + allocationId));
    }

    /**
     * Executes to response for `SubscriptionAllocationService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param allocation input consumed by toResponse.
     * @return result produced by toResponse.
     */
    private SubscriptionAllocationResponse toResponse(SubscriptionAllocation allocation) {
        return new SubscriptionAllocationResponse(
                allocation.getId(),
                allocation.getPlan().getId(),
                allocation.getPlan().getCode(),
                allocation.getPlan().getName(),
                allocation.getPlan().getCurrencyCode(),
                allocation.getAllocationType(),
                allocation.getUserId(),
                allocation.getOrganizationId(),
                allocation.getGroupId(),
                allocation.getQuotaLimit(),
                allocation.getEffectiveQuotaLimit(),
                allocation.getConsumedUnits(),
                allocation.getRemainingQuota(),
                allocation.getStartsAt(),
                allocation.getEndsAt(),
                allocation.getStatus(),
                allocation.getCreatedBy(),
                allocation.getCreatedAt(),
                allocation.getUpdatedAt()
        );
    }

    /**
     * Validates validate target for `SubscriptionAllocationService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param request input consumed by validateTarget.
     */
    private void validateTarget(CreateSubscriptionAllocationRequest request) {
        switch (request.allocationType()) {
            case USER -> {
                if (request.userId() == null) {
                    throw new IllegalArgumentException("userId is required for USER allocations");
                }
            }
            case ORGANIZATION -> {
                if (request.organizationId() == null) {
                    throw new IllegalArgumentException("organizationId is required for ORGANIZATION allocations");
                }
            }
            case ORGANIZATION_GROUP -> {
                if (request.organizationId() == null || request.groupId() == null) {
                    throw new IllegalArgumentException("organizationId and groupId are required for ORGANIZATION_GROUP allocations");
                }
            }
        }
    }

    /**
     * Validates validate schedule for `SubscriptionAllocationService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param startsAt input consumed by validateSchedule.
     * @param endsAt input consumed by validateSchedule.
     */
    private void validateSchedule(OffsetDateTime startsAt, OffsetDateTime endsAt) {
        if (endsAt != null && endsAt.isBefore(startsAt)) {
            throw new IllegalArgumentException("Allocation end time must be after start time");
        }
    }

    /**
     * Creates build status detail for `SubscriptionAllocationService`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.service`.
     * @param request input consumed by buildStatusDetail.
     * @return result produced by buildStatusDetail.
     */
    private String buildStatusDetail(UpdateAllocationStatusRequest request) {
        String reason = request.reason() == null ? "" : request.reason().trim();
        if (reason.isBlank()) {
            return "Changed allocation status to " + request.status();
        }
        return "Changed allocation status to " + request.status() + ": " + reason;
    }

    /**
     * Executes normalize actor for `SubscriptionAllocationService`.
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
