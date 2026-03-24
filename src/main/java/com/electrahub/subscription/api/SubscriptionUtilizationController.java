package com.electrahub.subscription.api;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionUtilizationController.class);


    private final SubscriptionPricingService subscriptionPricingService;

    /**
     * Executes subscription utilization controller for `SubscriptionUtilizationController`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.api`.
     * @param subscriptionPricingService input consumed by SubscriptionUtilizationController.
     */
    public SubscriptionUtilizationController(SubscriptionPricingService subscriptionPricingService) {
        LOGGER.info("CODEx_ENTRY_LOG: Entering SubscriptionUtilizationController#SubscriptionUtilizationController");
        LOGGER.debug("CODEx_ENTRY_LOG: Entering SubscriptionUtilizationController#SubscriptionUtilizationController with debug context");
        this.subscriptionPricingService = subscriptionPricingService;
    }

    /**
     * Executes preview for `SubscriptionUtilizationController`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.api`.
     * @param request input consumed by preview.
     * @return result produced by preview.
     */
    @PostMapping("/preview")
    public SubscriptionUtilizationPreviewResponse preview(@Valid @RequestBody PreviewSubscriptionUtilizationRequest request) {
        return subscriptionPricingService.preview(request);
    }

    /**
     * Executes record for `SubscriptionUtilizationController`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.api`.
     * @param request input consumed by record.
     * @return result produced by record.
     */
    @PostMapping
    public SubscriptionUtilizationResponse record(@Valid @RequestBody RecordSubscriptionUtilizationRequest request) {
        return subscriptionPricingService.record(request);
    }

    /**
     * Retrieves list for `SubscriptionUtilizationController`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.api`.
     * @param userId input consumed by list.
     * @return result produced by list.
     */
    @GetMapping
    public List<SubscriptionUtilizationResponse> list(@RequestParam UUID userId) {
        return subscriptionPricingService.listByUser(userId);
    }
}
