package com.electrahub.subscription.service;

import com.electrahub.subscription.domain.AllocationStatus;
import com.electrahub.subscription.domain.AllocationType;
import com.electrahub.subscription.domain.AutoApplyPolicy;
import com.electrahub.subscription.domain.ChargingScopeType;
import com.electrahub.subscription.domain.DiscountType;
import com.electrahub.subscription.domain.SubscriptionAllocation;
import com.electrahub.subscription.domain.SubscriptionPlan;
import com.electrahub.subscription.repository.SubscriptionAllocationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubscriptionEligibilityServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void scopesRecommendationsAndWritesEveryCacheEntryWithPositiveTtl() {
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        SubscriptionAllocation global = allocation(userId, "GLOBAL", now);
        SubscriptionAllocation charger = allocation(userId, "CHARGER", now.plusSeconds(1));
        charger.configureEligibility(null, null, ChargingScopeType.CHARGER, "charger-42", AutoApplyPolicy.AUTO_APPLY);
        charger.recordChargingContext("charger-42", "location-7", "network-3");
        SubscriptionAllocation otherCharger = allocation(userId, "OTHER", now.plusSeconds(2));
        otherCharger.configureEligibility(null, null, ChargingScopeType.CHARGER, "charger-99", AutoApplyPolicy.AUTO_APPLY);

        SubscriptionAllocationRepository repository = mock(SubscriptionAllocationRepository.class);
        when(repository.findActiveUserAllocations(any(), any())).thenReturn(List.of(global, otherCharger, charger));
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(redis.getExpire(anyString(), any(TimeUnit.class))).thenReturn(-2L);
        when(redis.hasKey(anyString())).thenReturn(false);
        ObjectProvider<StringRedisTemplate> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(redis);
        ObjectProvider<ObjectMapper> mapperProvider = mock(ObjectProvider.class);
        when(mapperProvider.getIfAvailable(any())).thenReturn(new ObjectMapper().findAndRegisterModules());

        SubscriptionEligibilityService service = new SubscriptionEligibilityService(
                repository, provider, mapperProvider, Duration.ofSeconds(30), Duration.ZERO
        );

        var result = service.findEligible(
                userId, "charger-42", "location-7", "network-3", null, "US", true
        );

        assertThat(result).extracting(response -> response.planCode()).containsExactly("CHARGER", "GLOBAL");
        assertThat(result.getFirst().recommended()).isTrue();
        assertThat(result.getFirst().selectedByDefault()).isTrue();
        assertThat(result.getFirst().recommendationReason()).contains("last session");
        verify(values).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void rejectsRedisEntriesWithoutTtlAndFetchesRequiredDataFromPostgres() {
        UUID userId = UUID.randomUUID();
        SubscriptionAllocationRepository repository = mock(SubscriptionAllocationRepository.class);
        when(repository.findActiveUserAllocations(any(), any())).thenReturn(List.of());
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(redis.getExpire(anyString(), any(TimeUnit.class))).thenReturn(-1L);
        when(redis.hasKey(anyString())).thenReturn(true);
        ObjectProvider<StringRedisTemplate> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(redis);
        ObjectProvider<ObjectMapper> mapperProvider = mock(ObjectProvider.class);
        when(mapperProvider.getIfAvailable(any())).thenReturn(new ObjectMapper().findAndRegisterModules());

        SubscriptionEligibilityService service = new SubscriptionEligibilityService(
                repository, provider, mapperProvider, Duration.ofSeconds(30), Duration.ZERO
        );
        assertThat(service.findEligible(userId, "charger", null, null, null, null, false)).isEmpty();

        verify(redis).delete(anyString());
        verify(repository).findActiveUserAllocations(any(), any());
        verify(values).set(anyString(), anyString(), any(Duration.class));
    }

    private SubscriptionAllocation allocation(UUID userId, String code, OffsetDateTime createdAt) {
        SubscriptionPlan plan = new SubscriptionPlan(
                UUID.randomUUID(), code, code + " plan", "Eligibility test", "USD",
                DiscountType.PERCENTAGE, new BigDecimal("100"), DiscountType.NONE,
                BigDecimal.ZERO, 100, true, createdAt
        );
        return new SubscriptionAllocation(
                UUID.randomUUID(), plan, AllocationType.USER, userId, null, null,
                100, createdAt.minusDays(1), createdAt.plusDays(30), AllocationStatus.ACTIVE,
                "test", createdAt
        );
    }
}
