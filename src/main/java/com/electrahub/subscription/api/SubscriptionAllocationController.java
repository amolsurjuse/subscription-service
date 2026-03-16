package com.electrahub.subscription.api;

import com.electrahub.subscription.api.dto.CreateSubscriptionAllocationRequest;
import com.electrahub.subscription.api.dto.SubscriptionAllocationResponse;
import com.electrahub.subscription.api.dto.UpdateAllocationStatusRequest;
import com.electrahub.subscription.service.SubscriptionAllocationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscriptions/allocations")
public class SubscriptionAllocationController {

    private final SubscriptionAllocationService subscriptionAllocationService;

    public SubscriptionAllocationController(SubscriptionAllocationService subscriptionAllocationService) {
        this.subscriptionAllocationService = subscriptionAllocationService;
    }

    @PostMapping
    public SubscriptionAllocationResponse create(@Valid @RequestBody CreateSubscriptionAllocationRequest request) {
        return subscriptionAllocationService.create(request);
    }

    @GetMapping
    public List<SubscriptionAllocationResponse> list(@RequestParam(required = false) UUID userId,
                                                     @RequestParam(required = false) UUID organizationId,
                                                     @RequestParam(required = false) UUID groupId,
                                                     @RequestParam(required = false) Boolean activeOnly) {
        return subscriptionAllocationService.list(userId, organizationId, groupId, activeOnly);
    }

    @PatchMapping("/{allocationId}/status")
    public SubscriptionAllocationResponse updateStatus(@PathVariable UUID allocationId,
                                                       @Valid @RequestBody UpdateAllocationStatusRequest request) {
        return subscriptionAllocationService.updateStatus(allocationId, request);
    }
}
