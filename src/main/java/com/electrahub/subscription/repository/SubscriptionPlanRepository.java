package com.electrahub.subscription.repository;

import com.electrahub.subscription.domain.SubscriptionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Page<SubscriptionPlan> findAllByOrderByUpdatedAtDesc(Pageable pageable);
}
