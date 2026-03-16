package com.electrahub.subscription.api;

import com.electrahub.subscription.api.dto.CreateSubscriptionPlanRequest;
import com.electrahub.subscription.api.dto.SubscriptionPlanResponse;
import com.electrahub.subscription.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscriptions/plans")
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    public SubscriptionPlanController(SubscriptionPlanService subscriptionPlanService) {
        this.subscriptionPlanService = subscriptionPlanService;
    }

    @PostMapping
    public SubscriptionPlanResponse create(@Valid @RequestBody CreateSubscriptionPlanRequest request) {
        return subscriptionPlanService.create(request);
    }

    @GetMapping
    public List<SubscriptionPlanResponse> list() {
        return subscriptionPlanService.list();
    }

    @GetMapping("/{planId}")
    public SubscriptionPlanResponse get(@PathVariable UUID planId) {
        return subscriptionPlanService.get(planId);
    }
}
