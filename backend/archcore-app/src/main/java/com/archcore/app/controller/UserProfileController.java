package com.archcore.app.controller;

import com.archcore.app.dto.UserProfileResponse;
import com.archcore.app.dto.UserProfileUpdateRequest;
import com.archcore.core.domain.UserProfile;
import com.archcore.core.service.UserProfileService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/profile")
public class UserProfileController {

    private static final Logger log = LoggerFactory.getLogger(UserProfileController.class);

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        UserProfile profile = userProfileService.getProfile(userId);

        return ResponseEntity.ok(toResponse(profile));
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserProfileUpdateRequest request) {

        String userId = jwt.getSubject();
        UserProfile updated = userProfileService.updateProfile(
                userId,
                request.firstName(),
                request.lastName(),
                request.profilePictureUrl()
        );

        return ResponseEntity.ok(toResponse(updated));
    }

    private UserProfileResponse toResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.getUserId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getProfilePictureUrl(),
                profile.getFullName().trim(),
                profile.isAccountActive(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
