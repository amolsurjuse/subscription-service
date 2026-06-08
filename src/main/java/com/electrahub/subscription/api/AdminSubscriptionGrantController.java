package com.electrahub.subscription.api;

import com.electrahub.subscription.api.dto.CreateSubscriptionGrantRequest;
import com.electrahub.subscription.api.dto.SubscriptionAllocationResponse;
import com.electrahub.subscription.service.SubscriptionAllocationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/subscriptions/grants")
public class AdminSubscriptionGrantController {

    private final SubscriptionAllocationService subscriptionAllocationService;

    public AdminSubscriptionGrantController(SubscriptionAllocationService subscriptionAllocationService) {
        this.subscriptionAllocationService = subscriptionAllocationService;
    }

    @PostMapping
    public SubscriptionAllocationResponse grant(@Valid @RequestBody CreateSubscriptionGrantRequest request) {
        return subscriptionAllocationService.grantToUser(request);
    }
}
