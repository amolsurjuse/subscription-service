package com.electrahub.subscription.repository;

import com.electrahub.subscription.domain.SubscriptionUtilization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubscriptionUtilizationRepository extends JpaRepository<SubscriptionUtilization, UUID> {

    /**
     * Retrieves find top100 by user id order by utilized at desc for `SubscriptionUtilizationRepository`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.repository`.
     * @param userId input consumed by findTop100ByUserIdOrderByUtilizedAtDesc.
     * @return result produced by findTop100ByUserIdOrderByUtilizedAtDesc.
     */
    List<SubscriptionUtilization> findTop100ByUserIdOrderByUtilizedAtDesc(UUID userId);
}
