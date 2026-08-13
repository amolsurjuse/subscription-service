package com.electrahub.subscription.service;

import com.electrahub.subscription.api.dto.EligibleSubscriptionResponse;
import com.electrahub.subscription.domain.AutoApplyPolicy;
import com.electrahub.subscription.domain.ChargingScopeType;
import com.electrahub.subscription.domain.SubscriptionAllocation;
import com.electrahub.subscription.repository.SubscriptionAllocationRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class SubscriptionEligibilityService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionEligibilityService.class);
    private static final TypeReference<List<EligibleSubscriptionResponse>> RESPONSE_TYPE = new TypeReference<>() {};

    private final SubscriptionAllocationRepository repository;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final Duration ttl;
    private final Duration jitter;

    public SubscriptionEligibilityService(SubscriptionAllocationRepository repository,
                                          ObjectProvider<StringRedisTemplate> redisProvider,
                                          ObjectProvider<ObjectMapper> objectMapperProvider,
                                          @Value("${app.eligibility-cache.ttl:30s}") Duration ttl,
                                          @Value("${app.eligibility-cache.jitter:3s}") Duration jitter) {
        this.repository = repository;
        this.redis = redisProvider.getIfAvailable();
        this.objectMapper = objectMapperProvider.getIfAvailable(
                () -> new ObjectMapper().findAndRegisterModules()
        );
        this.ttl = ttl;
        this.jitter = jitter;
    }

    @Transactional(readOnly = true)
    public List<EligibleSubscriptionResponse> findEligible(UUID userId,
                                                            String chargerId,
                                                            String locationId,
                                                            String networkId,
                                                            String enterpriseId,
                                                            String countryCode,
                                                            boolean plugAndCharge) {
        String key = cacheKey(userId, chargerId, locationId, networkId, enterpriseId, countryCode, String.valueOf(plugAndCharge));
        List<EligibleSubscriptionResponse> cached = readCache(key);
        if (cached != null) {
            return cached;
        }

        OffsetDateTime now = OffsetDateTime.now();
        List<SubscriptionAllocation> matches = repository.findActiveUserAllocations(userId, now).stream()
                .filter(allocation -> allocation.getRemainingQuotaValue() == null
                        || allocation.getRemainingQuotaValue().signum() > 0)
                .filter(allocation -> matchesScope(allocation, chargerId, locationId, networkId, enterpriseId, countryCode))
                .sorted(Comparator
                        .comparing((SubscriptionAllocation allocation) -> recommendationRank(allocation, chargerId, locationId, networkId))
                        .thenComparing(SubscriptionAllocation::getLastUsedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(SubscriptionAllocation::getCreatedAt))
                .toList();

        List<EligibleSubscriptionResponse> response = new ArrayList<>(matches.size());
        for (int index = 0; index < matches.size(); index++) {
            SubscriptionAllocation allocation = matches.get(index);
            boolean recommended = index == 0;
            boolean selectedByDefault = recommended
                    && allocation.getAutoApplyPolicy() == AutoApplyPolicy.AUTO_APPLY
                    && (!plugAndCharge || allocation.getAutoApplyPolicy() == AutoApplyPolicy.AUTO_APPLY);
            response.add(new EligibleSubscriptionResponse(
                    allocation.getId(),
                    allocation.getPlan().getId(),
                    allocation.getPlan().getCode(),
                    allocation.getPlan().getName(),
                    recommended,
                    selectedByDefault,
                    recommendationReason(allocation, chargerId, locationId, networkId),
                    allocation.getRemainingQuotaValue(),
                    allocation.getPlan().getQuotaUnit(),
                    allocation.getChargingScopeType(),
                    allocation.getChargingScopeReference(),
                    allocation.getAutoApplyPolicy(),
                    allocation.getEndsAt()
            ));
        }
        writeCache(key, response);
        return List.copyOf(response);
    }

    private boolean matchesScope(SubscriptionAllocation allocation,
                                 String chargerId,
                                 String locationId,
                                 String networkId,
                                 String enterpriseId,
                                 String countryCode) {
        String expected = normalize(allocation.getChargingScopeReference());
        return switch (allocation.getChargingScopeType()) {
            case ALL_CHARGERS -> true;
            case CHARGER -> equalsNormalized(expected, chargerId);
            case LOCATION -> equalsNormalized(expected, locationId);
            case NETWORK -> equalsNormalized(expected, networkId);
            case ENTERPRISE -> equalsNormalized(expected, enterpriseId);
            case COUNTRY -> equalsNormalized(expected, countryCode);
            case CHARGER_GROUP -> false; // enabled when charger-group projection is connected
        };
    }

    private int recommendationRank(SubscriptionAllocation allocation, String chargerId, String locationId, String networkId) {
        if (equalsNormalized(allocation.getLastUsedChargerId(), chargerId)) return 0;
        if (equalsNormalized(allocation.getLastUsedLocationId(), locationId)) return 1;
        if (equalsNormalized(allocation.getLastUsedNetworkId(), networkId)) return 2;
        return scopeRank(allocation.getChargingScopeType()) + 10;
    }

    private int scopeRank(ChargingScopeType type) {
        return switch (type) {
            case CHARGER -> 0;
            case LOCATION -> 1;
            case CHARGER_GROUP -> 2;
            case NETWORK -> 3;
            case ENTERPRISE -> 4;
            case COUNTRY -> 5;
            case ALL_CHARGERS -> 6;
        };
    }

    private String recommendationReason(SubscriptionAllocation allocation, String chargerId, String locationId, String networkId) {
        if (equalsNormalized(allocation.getLastUsedChargerId(), chargerId)) return "Used for your last session at this charger";
        if (equalsNormalized(allocation.getLastUsedLocationId(), locationId)) return "Used for your last session at this location";
        if (equalsNormalized(allocation.getLastUsedNetworkId(), networkId)) return "Used for your last session on this network";
        return "Best eligible subscription for this charger";
    }

    private List<EligibleSubscriptionResponse> readCache(String key) {
        if (redis == null) return null;
        try {
            Long remainingTtl = redis.getExpire(key, TimeUnit.MILLISECONDS);
            if (remainingTtl == null || remainingTtl <= 0) {
                if (Boolean.TRUE.equals(redis.hasKey(key))) {
                    redis.delete(key);
                }
                return null;
            }
            String value = redis.opsForValue().get(key);
            return value == null ? null : objectMapper.readValue(value, RESPONSE_TYPE);
        } catch (Exception error) {
            LOGGER.warn("Eligibility Redis read failed; using PostgreSQL read-through", error);
            return null;
        }
    }

    private void writeCache(String key, List<EligibleSubscriptionResponse> value) {
        if (redis == null) return;
        try {
            long jitterMillis = jitter.isZero() ? 0 : ThreadLocalRandom.current().nextLong(jitter.toMillis() + 1);
            Duration expiringTtl = ttl.plusMillis(jitterMillis);
            redis.opsForValue().set(key, objectMapper.writeValueAsString(value), expiringTtl);
        } catch (Exception error) {
            LOGGER.warn("Eligibility Redis write failed; response remains valid from PostgreSQL", error);
        }
    }

    private String cacheKey(UUID userId, String... context) {
        StringBuilder key = new StringBuilder("subscription:eligibility:{").append(userId).append("}");
        for (String value : context) key.append(':').append(normalize(value) == null ? "_" : normalize(value));
        return key.toString();
    }

    private boolean equalsNormalized(String left, String right) {
        String normalizedLeft = normalize(left);
        String normalizedRight = normalize(right);
        return normalizedLeft != null && normalizedLeft.equalsIgnoreCase(normalizedRight);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
