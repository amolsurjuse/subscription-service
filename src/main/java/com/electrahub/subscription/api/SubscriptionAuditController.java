package com.electrahub.subscription.api;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.electrahub.subscription.api.dto.PagedResponse;
import com.electrahub.subscription.api.dto.SubscriptionAuditLogResponse;
import com.electrahub.subscription.domain.AuditAction;
import com.electrahub.subscription.service.SubscriptionAuditQueryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscriptions/audit-logs")
public class SubscriptionAuditController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionAuditController.class);


    private final SubscriptionAuditQueryService subscriptionAuditQueryService;

    /**
     * Executes subscription audit controller for `SubscriptionAuditController`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.api`.
     * @param subscriptionAuditQueryService input consumed by SubscriptionAuditController.
     */
    public SubscriptionAuditController(SubscriptionAuditQueryService subscriptionAuditQueryService) {
        LOGGER.info(" Entering SubscriptionAuditController#SubscriptionAuditController");
        LOGGER.debug(" Entering SubscriptionAuditController#SubscriptionAuditController with debug context");
        this.subscriptionAuditQueryService = subscriptionAuditQueryService;
    }

    @GetMapping
    public List<SubscriptionAuditLogResponse> search(@RequestParam(required = false) UUID planId,
                                                     /**
                                                      * Executes request param for `SubscriptionAuditController`.
                                                      *
                                                      * <p>Detailed behavior: follows the current implementation path and
                                                      * enforces component-specific rules in `com.electrahub.subscription.api`.
                                                      * @param groupId input consumed by RequestParam.
                                                      * @return result produced by RequestParam.
                                                      */
                                                     @RequestParam(required = false) UUID allocationId,
                                                     @RequestParam(required = false) UUID userId,
                                                     @RequestParam(required = false) UUID organizationId,
                                                     @RequestParam(required = false) UUID groupId) {
        return subscriptionAuditQueryService.search(planId, allocationId, userId, organizationId, groupId);
    }

    @GetMapping("/paged")
    public PagedResponse<SubscriptionAuditLogResponse> searchPaged(@RequestParam(required = false) UUID planId,
                                                                   @RequestParam(required = false) UUID allocationId,
                                                                   @RequestParam(required = false) UUID userId,
                                                                   @RequestParam(required = false) UUID organizationId,
                                                                   @RequestParam(required = false) UUID groupId,
                                                                   @RequestParam(required = false) AuditAction action,
                                                                   @RequestParam(required = false) String entityType,
                                                                   @RequestParam(required = false) String query,
                                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdFrom,
                                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdTo,
                                                                   @RequestParam(defaultValue = "15") @Min(1) @Max(200) int limit,
                                                                   @RequestParam(defaultValue = "0") @Min(0) int offset) {
        return subscriptionAuditQueryService.searchPaged(
                planId,
                allocationId,
                userId,
                organizationId,
                groupId,
                action,
                entityType,
                query,
                createdFrom,
                createdTo,
                limit,
                offset
        );
    }
}
