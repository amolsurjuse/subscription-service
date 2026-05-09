package com.electrahub.subscription.api;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.electrahub.subscription.api.dto.SubscriptionAuditLogResponse;
import com.electrahub.subscription.service.SubscriptionAuditQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
