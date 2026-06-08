package com.electrahub.subscription.api;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.electrahub.subscription.api.dto.CreateSubscriptionAllocationRequest;
import com.electrahub.subscription.api.dto.PagedResponse;
import com.electrahub.subscription.api.dto.SubscriptionAllocationResponse;
import com.electrahub.subscription.api.dto.UpdateAllocationStatusRequest;
import com.electrahub.subscription.domain.AllocationStatus;
import com.electrahub.subscription.domain.AllocationSource;
import com.electrahub.subscription.domain.AllocationType;
import com.electrahub.subscription.service.SubscriptionAllocationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionAllocationController.class);


    private final SubscriptionAllocationService subscriptionAllocationService;

    /**
     * Executes subscription allocation controller for `SubscriptionAllocationController`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.api`.
     * @param subscriptionAllocationService input consumed by SubscriptionAllocationController.
     */
    public SubscriptionAllocationController(SubscriptionAllocationService subscriptionAllocationService) {
        LOGGER.info(" Entering SubscriptionAllocationController#SubscriptionAllocationController");
        LOGGER.debug(" Entering SubscriptionAllocationController#SubscriptionAllocationController with debug context");
        this.subscriptionAllocationService = subscriptionAllocationService;
    }

    /**
     * Creates create for `SubscriptionAllocationController`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.api`.
     * @param request input consumed by create.
     * @return result produced by create.
     */
    @PostMapping
    public SubscriptionAllocationResponse create(@Valid @RequestBody CreateSubscriptionAllocationRequest request) {
        return subscriptionAllocationService.create(request);
    }

    @GetMapping
    public List<SubscriptionAllocationResponse> list(@RequestParam(required = false) UUID userId,
                                                     /**
                                                      * Executes request param for `SubscriptionAllocationController`.
                                                      *
                                                      * <p>Detailed behavior: follows the current implementation path and
                                                      * enforces component-specific rules in `com.electrahub.subscription.api`.
                                                      * @param activeOnly input consumed by RequestParam.
                                                      * @return result produced by RequestParam.
                                                      */
                                                     @RequestParam(required = false) UUID organizationId,
                                                     @RequestParam(required = false) UUID groupId,
                                                     @RequestParam(required = false) Boolean activeOnly) {
        return subscriptionAllocationService.list(userId, organizationId, groupId, activeOnly);
    }

    @GetMapping("/paged")
    public PagedResponse<SubscriptionAllocationResponse> listPaged(@RequestParam(required = false) UUID userId,
                                                                   @RequestParam(required = false) UUID organizationId,
                                                                   @RequestParam(required = false) UUID groupId,
                                                                   @RequestParam(required = false) UUID planId,
                                                                   @RequestParam(required = false) AllocationType allocationType,
                                                                   @RequestParam(required = false) AllocationStatus status,
                                                                   @RequestParam(required = false) AllocationSource source,
                                                                   @RequestParam(required = false) UUID enterpriseId,
                                                                   @RequestParam(required = false) Boolean exhausted,
                                                                   @RequestParam(required = false) Boolean activeOnly,
                                                                   @RequestParam(defaultValue = "10") @Min(1) @Max(200) int limit,
                                                                   @RequestParam(defaultValue = "0") @Min(0) int offset) {
        return subscriptionAllocationService.listPaged(
                userId,
                organizationId,
                groupId,
                planId,
                allocationType,
                status,
                source,
                enterpriseId,
                exhausted,
                activeOnly,
                limit,
                offset
        );
    }

    @PatchMapping("/{allocationId}/status")
    public SubscriptionAllocationResponse updateStatus(@PathVariable UUID allocationId,
                                                       @Valid @RequestBody UpdateAllocationStatusRequest request) {
        return subscriptionAllocationService.updateStatus(allocationId, request);
    }
}
