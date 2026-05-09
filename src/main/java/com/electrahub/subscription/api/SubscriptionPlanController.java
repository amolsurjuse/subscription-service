package com.electrahub.subscription.api;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.electrahub.subscription.api.dto.CreateSubscriptionPlanRequest;
import com.electrahub.subscription.api.dto.SubscriptionPlanResponse;
import com.electrahub.subscription.api.dto.SubscriptionPlanSearchResponse;
import com.electrahub.subscription.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscriptions/plans")
public class SubscriptionPlanController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionPlanController.class);


    private final SubscriptionPlanService subscriptionPlanService;

    /**
     * Executes subscription plan controller for `SubscriptionPlanController`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.api`.
     * @param subscriptionPlanService input consumed by SubscriptionPlanController.
     */
    public SubscriptionPlanController(SubscriptionPlanService subscriptionPlanService) {
        LOGGER.info(" Entering SubscriptionPlanController#SubscriptionPlanController");
        LOGGER.debug(" Entering SubscriptionPlanController#SubscriptionPlanController with debug context");
        this.subscriptionPlanService = subscriptionPlanService;
    }

    /**
     * Creates create for `SubscriptionPlanController`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.api`.
     * @param request input consumed by create.
     * @return result produced by create.
     */
    @PostMapping
    public SubscriptionPlanResponse create(@Valid @RequestBody CreateSubscriptionPlanRequest request) {
        return subscriptionPlanService.create(request);
    }

    @GetMapping
    public SubscriptionPlanSearchResponse list(
            @RequestParam(defaultValue = "10") @Min(1) @Max(200) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset
    ) {
        return subscriptionPlanService.list(limit, offset);
    }

    /**
     * Retrieves get for `SubscriptionPlanController`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.api`.
     * @param planId input consumed by get.
     * @return result produced by get.
     */
    @GetMapping("/{planId}")
    public SubscriptionPlanResponse get(@PathVariable UUID planId) {
        return subscriptionPlanService.get(planId);
    }
}
