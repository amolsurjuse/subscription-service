package com.electrahub.subscription.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Locale;
import java.util.UUID;

@Component
public class UserCountryClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserCountryClient.class);

    private final RestClient restClient;

    public UserCountryClient(
            @Value("${app.services.user.url:http://user-service:8082}") String userServiceUrl
    ) {
        this.restClient = RestClient.create(userServiceUrl);
    }

    public String requireCountry(UUID userId) {
        try {
            BillingProfile response = restClient.get()
                    .uri("/api/internal/users/{userId}/billing-profile", userId)
                    .retrieve()
                    .body(BillingProfile.class);
            String country = normalizeCountry(response == null ? null : response.countryCode());
            if (country == null) {
                throw new IllegalStateException("User profile does not have a country.");
            }
            return country;
        } catch (RuntimeException error) {
            LOGGER.warn("Unable to resolve country for subscription user {}", userId, error);
            throw new IllegalStateException("User country could not be verified.", error);
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
}
