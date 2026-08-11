package com.archcore.core.repository;

import com.archcore.core.domain.Subscription;
import com.archcore.core.domain.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByUserIdAndStatus(String userId, SubscriptionStatus status);

    List<Subscription> findByUserId(String userId);

    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId AND s.status = 'ACTIVE' ORDER BY s.createdAt DESC LIMIT 1")
    Optional<Subscription> findActiveSubscriptionByUserId(@Param("userId") String userId);

    boolean existsByUserIdAndStatus(String userId, SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'PAST_DUE' AND s.billingCycleEnd < CURRENT_TIMESTAMP")
    List<Subscription> findPastDueSubscriptions();
}
