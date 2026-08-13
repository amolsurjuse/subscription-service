package com.electrahub.subscription.api;

import com.electrahub.subscription.api.dto.DriverSubscriptionPlanResponse;
import com.electrahub.subscription.api.dto.DriverSubscriptionResponse;
import com.electrahub.subscription.api.dto.DriverSubscribeRequest;
import com.electrahub.subscription.api.dto.EligibleSubscriptionResponse;
import com.electrahub.subscription.service.AccountContextResolver;
import com.electrahub.subscription.service.SubscriptionAllocationService;
import com.electrahub.subscription.service.SubscriptionEligibilityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/driver/subscriptions")
public class DriverSubscriptionController {

    private final AccountContextResolver accountContextResolver;
    private final SubscriptionAllocationService subscriptionAllocationService;
    private final SubscriptionEligibilityService subscriptionEligibilityService;

    public DriverSubscriptionController(AccountContextResolver accountContextResolver,
                                        SubscriptionAllocationService subscriptionAllocationService,
                                        SubscriptionEligibilityService subscriptionEligibilityService) {
        this.accountContextResolver = accountContextResolver;
        this.subscriptionAllocationService = subscriptionAllocationService;
        this.subscriptionEligibilityService = subscriptionEligibilityService;
    }

    @GetMapping("/eligible")
    public List<EligibleSubscriptionResponse> eligibleSubscriptions(
            @RequestParam String chargerId,
            @RequestParam(required = false) String locationId,
            @RequestParam(required = false) String networkId,
            @RequestParam(required = false) String enterpriseId,
            @RequestParam(required = false) String countryCode,
            @RequestParam(defaultValue = "false") boolean plugAndCharge,
            HttpServletRequest request
    ) {
        requireCustomer(request);
        UUID userId = UUID.fromString(accountContextResolver.resolveAccountId(request));
        return subscriptionEligibilityService.findEligible(
                userId, chargerId, locationId, networkId, enterpriseId, countryCode, plugAndCharge
        );
    }

    @GetMapping("/me")
    public List<DriverSubscriptionResponse> mySubscriptions(
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            HttpServletRequest request
    ) {
        requireCustomer(request);
        UUID userId = UUID.fromString(accountContextResolver.resolveAccountId(request));
        return subscriptionAllocationService.listDriverSubscriptions(userId, activeOnly, limit, offset);
    }

    @PostMapping
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public DriverSubscriptionResponse subscribe(@Valid @RequestBody DriverSubscribeRequest body,
                                                HttpServletRequest request) {
        requireCustomer(request);
        UUID userId = UUID.fromString(accountContextResolver.resolveAccountId(request));
        return subscriptionAllocationService.selfSubscribe(userId, body.planId());
    }

    @GetMapping("/plans")
    public List<DriverSubscriptionPlanResponse> plans(
            @RequestParam(defaultValue = "US") String countryCode,
            @RequestParam(defaultValue = "USD") String currency,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            HttpServletRequest request
    ) {
        requireCustomer(request);
        return subscriptionAllocationService.listDriverPlans(countryCode, currency, limit, offset);
    }

    private void requireCustomer(HttpServletRequest request) {
        if (!accountContextResolver.hasRole(request, "CUSTOMER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only customer accounts can access driver subscriptions.");
        }
    }
}
