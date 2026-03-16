package com.electrahub.subscription.api;

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

    private final SubscriptionAuditQueryService subscriptionAuditQueryService;

    public SubscriptionAuditController(SubscriptionAuditQueryService subscriptionAuditQueryService) {
        this.subscriptionAuditQueryService = subscriptionAuditQueryService;
    }

    @GetMapping
    public List<SubscriptionAuditLogResponse> search(@RequestParam(required = false) UUID planId,
                                                     @RequestParam(required = false) UUID allocationId,
                                                     @RequestParam(required = false) UUID userId,
                                                     @RequestParam(required = false) UUID organizationId,
                                                     @RequestParam(required = false) UUID groupId) {
        return subscriptionAuditQueryService.search(planId, allocationId, userId, organizationId, groupId);
    }
}
