package com.archcore.app.controller;

import com.archcore.app.audit.LogActivity;
import com.archcore.app.ratelimit.RateLimit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class TestDomainController {

    private static final Logger log = LoggerFactory.getLogger(TestDomainController.class);

    @GetMapping("/public/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());
        response.put("service", "ArchCore SaaS Kit");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> response = new HashMap<>();
        response.put("userId", jwt.getSubject());
        response.put("email", jwt.getClaimAsString("email"));
        response.put("firstName", jwt.getClaimAsString("given_name"));
        response.put("lastName", jwt.getClaimAsString("family_name"));
        response.put("roles", extractRealmRoles(jwt));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimit(requests = 10, periodSeconds = 60, scope = RateLimit.RateLimitScope.USER)
    @LogActivity(description = "Admin Dashboard Accessed")
    public ResponseEntity<Map<String, Object>> getAdminDashboard(@AuthenticationPrincipal Jwt jwt) {
        log.info("Admin dashboard accessed by user: {}", jwt.getSubject());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to the Admin Dashboard");
        response.put("adminUser", jwt.getSubject());
        response.put("timestamp", Instant.now().toString());
        response.put("systemInfo", Map.of(
                "version", "1.0.0",
                "environment", "development"
        ));
        return ResponseEntity.ok(response);
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null) {
            return (List<String>) realmAccess.getOrDefault("roles", List.of());
        }
        return List.of();
    }
}
