package com.archcore.app.controller;

import com.archcore.app.audit.LogActivity;
import com.archcore.app.dto.UserProfileResponse;
import com.archcore.app.ratelimit.RateLimit;
import com.archcore.core.domain.Plan;
import com.archcore.core.domain.Subscription;
import com.archcore.core.domain.UserProfile;
import com.archcore.core.domain.enums.PlanTier;
import com.archcore.core.service.BillingService;
import com.archcore.core.service.UserProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sample")
public class SampleDomainController {

    private static final Logger log = LoggerFactory.getLogger(SampleDomainController.class);

    private final BillingService billingService;
    private final UserProfileService userProfileService;

    public SampleDomainController(BillingService billingService, UserProfileService userProfileService) {
        this.billingService = billingService;
        this.userProfileService = userProfileService;
    }

    @GetMapping("/dashboard")
    @LogActivity(description = "User accessed dashboard")
    @RateLimit(requests = 30, periodSeconds = 60, scope = RateLimit.RateLimitScope.USER)
    public ResponseEntity<Map<String, Object>> getDashboard(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();

        UserProfile profile = userProfileService.getOrCreateProfile(
                userId,
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name")
        );

        Subscription subscription = billingService.getActiveSubscription(userId).orElse(null);
        Plan plan = subscription != null ? subscription.getPlan() : null;

        Map<String, Object> response = new HashMap<>();
        response.put("user", Map.of(
                "id", userId,
                "name", profile.getFullName().trim(),
                "email", jwt.getClaimAsString("email") != null ? jwt.getClaimAsString("email") : ""
        ));
        response.put("subscription", Map.of(
                "plan", plan != null ? plan.getTier().name() : "FREE",
                "status", subscription != null ? subscription.getStatus().name() : "NONE",
                "rateLimit", plan != null ? plan.getRateLimitPerMinute() : 60
        ));
        response.put("features", Map.of(
                "maxProjects", plan != null ? plan.getMaxProjects() : 3,
                "maxMembers", plan != null ? plan.getMaxMembers() : 1
        ));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/project/create")
    @LogActivity(description = "Project created")
    @RateLimit(requests = 10, periodSeconds = 60, scope = RateLimit.RateLimitScope.USER)
    public ResponseEntity<Map<String, Object>> createProject(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String projectName) {

        String userId = jwt.getSubject();
        log.info("Creating project '{}' for user {}", projectName, userId);

        return ResponseEntity.ok(Map.of(
                "projectId", java.util.UUID.randomUUID().toString(),
                "projectName", projectName,
                "createdBy", userId,
                "status", "created"
        ));
    }

    @GetMapping("/activity")
    @LogActivity(description = "Viewed activity history")
    @RateLimit(requests = 20, periodSeconds = 60, scope = RateLimit.RateLimitScope.USER)
    public ResponseEntity<Map<String, Object>> getActivity(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Fetching activity for user: {}", userId);

        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "message", "Activity history retrieved successfully"
        ));
    }
}
