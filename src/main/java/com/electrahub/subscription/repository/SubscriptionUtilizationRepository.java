package com.electrahub.subscription.repository;

import com.electrahub.subscription.domain.SubscriptionUtilization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubscriptionUtilizationRepository extends JpaRepository<SubscriptionUtilization, UUID> {

    List<SubscriptionUtilization> findTop100ByUserIdOrderByUtilizedAtDesc(UUID userId);
}
