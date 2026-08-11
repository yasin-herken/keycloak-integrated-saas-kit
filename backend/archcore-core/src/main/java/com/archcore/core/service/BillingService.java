package com.archcore.core.service;

import com.archcore.core.domain.Plan;
import com.archcore.core.domain.Subscription;
import com.archcore.core.domain.enums.PlanTier;
import com.archcore.core.domain.enums.SubscriptionStatus;
import com.archcore.core.repository.PlanRepository;
import com.archcore.core.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;

    public BillingService(PlanRepository planRepository, SubscriptionRepository subscriptionRepository) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public Plan getPlanByTier(PlanTier tier) {
        return planRepository.findByTier(tier)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found for tier: " + tier));
    }

    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    public Optional<Subscription> getActiveSubscription(String userId) {
        return subscriptionRepository.findActiveSubscriptionByUserId(userId);
    }

    public List<Subscription> getUserSubscriptions(String userId) {
        return subscriptionRepository.findByUserId(userId);
    }

    public Subscription createSubscription(String userId, PlanTier planTier) {
        Plan plan = getPlanByTier(planTier);
        Instant now = Instant.now();
        Instant billingCycleEnd = now.plus(30, ChronoUnit.DAYS);

        Subscription subscription = new Subscription(userId, plan, SubscriptionStatus.ACTIVE,
                now, now, billingCycleEnd);

        Subscription saved = subscriptionRepository.save(subscription);
        log.info("Created subscription for user {} with plan {}", userId, planTier);
        return saved;
    }

    public Subscription upgradeSubscription(String userId, PlanTier newPlanTier) {
        Subscription current = getActiveSubscription(userId)
                .orElseThrow(() -> new IllegalArgumentException("No active subscription found for user: " + userId));

        Plan newPlan = getPlanByTier(newPlanTier);
        current.setPlan(newPlan);

        Subscription updated = subscriptionRepository.save(current);
        log.info("Upgraded subscription for user {} to plan {}", userId, newPlanTier);
        return updated;
    }

    public void cancelSubscription(String userId) {
        Subscription subscription = getActiveSubscription(userId)
                .orElseThrow(() -> new IllegalArgumentException("No active subscription found for user: " + userId));

        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscription.setAutoRenew(false);
        subscriptionRepository.save(subscription);
        log.info("Canceled subscription for user {}", userId);
    }

    public void handlePaymentSucceeded(String externalSubscriptionId, Instant nextBillingDate) {
        subscriptionRepository.findByUserId(externalSubscriptionId)
                .stream()
                .filter(s -> s.getExternalSubscriptionId().equals(externalSubscriptionId))
                .findFirst()
                .ifPresent(subscription -> {
                    subscription.setStatus(SubscriptionStatus.ACTIVE);
                    subscription.setBillingCycleStart(Instant.now());
                    subscription.setBillingCycleEnd(nextBillingDate);
                    subscriptionRepository.save(subscription);
                    log.info("Payment succeeded for subscription {}", externalSubscriptionId);
                });
    }

    public void handleSubscriptionDeleted(String externalSubscriptionId) {
        subscriptionRepository.findByUserId(externalSubscriptionId)
                .stream()
                .filter(s -> s.getExternalSubscriptionId().equals(externalSubscriptionId))
                .findFirst()
                .ifPresent(subscription -> {
                    subscription.setStatus(SubscriptionStatus.CANCELED);
                    subscription.setAutoRenew(false);
                    subscriptionRepository.save(subscription);
                    log.info("Subscription deleted: {}", externalSubscriptionId);
                });
    }

    public void initializeDefaultPlans() {
        if (!planRepository.existsByTier(PlanTier.FREE)) {
            planRepository.save(new Plan(PlanTier.FREE, "Free Plan",
                    "Basic features with limited usage", 60, 3, 1));
        }
        if (!planRepository.existsByTier(PlanTier.PRO)) {
            planRepository.save(new Plan(PlanTier.PRO, "Pro Plan",
                    "Advanced features for professionals", 1000, 50, 25));
        }
        if (!planRepository.existsByTier(PlanTier.ENTERPRISE)) {
            planRepository.save(new Plan(PlanTier.ENTERPRISE, "Enterprise Plan",
                    "Full features for large teams", 10000, Long.MAX_VALUE, Long.MAX_VALUE));
        }
    }
}
