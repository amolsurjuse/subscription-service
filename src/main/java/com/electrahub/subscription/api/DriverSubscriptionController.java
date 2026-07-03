package com.electrahub.subscription.api;

import com.electrahub.subscription.api.dto.DriverSubscriptionPlanResponse;
import com.electrahub.subscription.api.dto.DriverSubscriptionResponse;
import com.electrahub.subscription.service.AccountContextResolver;
import com.electrahub.subscription.service.SubscriptionAllocationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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

    public DriverSubscriptionController(AccountContextResolver accountContextResolver,
                                        SubscriptionAllocationService subscriptionAllocationService) {
        this.accountContextResolver = accountContextResolver;
        this.subscriptionAllocationService = subscriptionAllocationService;
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
