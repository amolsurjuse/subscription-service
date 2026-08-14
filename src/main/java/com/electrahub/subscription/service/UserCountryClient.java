package com.electrahub.subscription.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserCountryClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserCountryClient.class);

    private final RestClient userClient;
    private final RestClient paymentClient;

    @Autowired
    public UserCountryClient(
            @Value("${app.services.user.url:http://user-service:8082}") String userServiceUrl,
            @Value("${app.services.payment.url:http://payment-service:8083}") String paymentServiceUrl
    ) {
        this(RestClient.create(userServiceUrl), RestClient.create(paymentServiceUrl));
    }

    UserCountryClient(RestClient userClient, RestClient paymentClient) {
        this.userClient = userClient;
        this.paymentClient = paymentClient;
    }

    public String requireCountry(UUID userId) {
        return findCountry(userId)
                .orElseThrow(() -> new IllegalStateException("User country could not be verified."));
    }

    public Optional<String> findCountry(UUID userId) {
        String profileCountry = findProfileCountry(userId);
        if (profileCountry != null) {
            return Optional.of(profileCountry);
        }

        String walletCountry = findWalletCountry(userId);
        if (walletCountry != null) {
            LOGGER.info("Using wallet country for subscription user {} because profile country is unavailable", userId);
            return Optional.of(walletCountry);
        }

        LOGGER.warn("Subscription user {} has no country in either profile or wallet", userId);
        return Optional.empty();
    }

    private String findProfileCountry(UUID userId) {
        try {
            BillingProfile response = userClient.get()
                    .uri("/api/internal/users/{userId}/billing-profile", userId)
                    .retrieve()
                    .body(BillingProfile.class);
            String country = normalizeCountry(response == null ? null : response.countryCode());
            if (country == null) {
                LOGGER.warn("Subscription user {} does not have a country in their billing profile", userId);
            }
            return country;
        } catch (RuntimeException error) {
            LOGGER.warn("Unable to resolve profile country for subscription user {}; trying wallet", userId, error);
            return null;
        }
    }

    private String findWalletCountry(UUID userId) {
        try {
            WalletProfile response = paymentClient.get()
                    .uri("/api/v1/payment/wallet")
                    .header("X-Account-Id", userId.toString())
                    .retrieve()
                    .body(WalletProfile.class);
            return normalizeCountry(response == null ? null : response.countryCode());
        } catch (RuntimeException error) {
            LOGGER.warn("Unable to resolve wallet country for subscription user {}", userId, error);
            return null;
        }
    }

    static boolean matchesPlanCountry(String planCountry, String actualCountry) {
        String expected = normalizeCountry(planCountry);
        return expected == null || expected.equals(normalizeCountry(actualCountry));
    }

    static String normalizeCountry(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) return null;
        return countryCode.trim().toUpperCase(Locale.ROOT);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record BillingProfile(String countryCode) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WalletProfile(String countryCode) {
    }
}
