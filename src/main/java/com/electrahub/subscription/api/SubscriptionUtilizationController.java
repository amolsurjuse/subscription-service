package com.electrahub.subscription.api;

import com.electrahub.subscription.api.dto.PreviewSubscriptionUtilizationRequest;
import com.electrahub.subscription.api.dto.RecordSubscriptionUtilizationRequest;
import com.electrahub.subscription.api.dto.SubscriptionUtilizationPreviewResponse;
import com.electrahub.subscription.api.dto.SubscriptionUtilizationResponse;
import com.electrahub.subscription.service.SubscriptionPricingService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/v1/subscriptions/utilizations")
public class SubscriptionUtilizationController {

    private final SubscriptionPricingService subscriptionPricingService;

    public SubscriptionUtilizationController(SubscriptionPricingService subscriptionPricingService) {
        this.subscriptionPricingService = subscriptionPricingService;
    }

    @PostMapping("/preview")
    public SubscriptionUtilizationPreviewResponse preview(@Valid @RequestBody PreviewSubscriptionUtilizationRequest request) {
        return subscriptionPricingService.preview(request);
    }

    @PostMapping
    public SubscriptionUtilizationResponse record(@Valid @RequestBody RecordSubscriptionUtilizationRequest request) {
        return subscriptionPricingService.record(request);
    }

    @GetMapping
    public List<SubscriptionUtilizationResponse> list(@RequestParam UUID userId) {
        return subscriptionPricingService.listByUser(userId);
    }
}
