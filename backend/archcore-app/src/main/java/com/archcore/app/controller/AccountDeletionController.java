package com.archcore.app.controller;

import com.archcore.app.dto.AccountDeletionRequest;
import com.archcore.app.dto.AccountDeletionResponse;
import com.archcore.core.service.UserProfileService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/users/account")
public class AccountDeletionController {

    private static final Logger log = LoggerFactory.getLogger(AccountDeletionController.class);

    private final UserProfileService userProfileService;

    public AccountDeletionController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PostMapping("/deactivate")
    public ResponseEntity<AccountDeletionResponse> deactivateAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AccountDeletionRequest request) {

        String userId = jwt.getSubject();

        if (!request.confirmDeletion()) {
            return ResponseEntity.badRequest().body(new AccountDeletionResponse(
                    "Account deletion not confirmed. Set confirmDeletion to true to proceed.",
                    "rejected",
                    Instant.now()
            ));
        }

        userProfileService.deactivateAccount(userId, request.reason());

        log.info("Account deactivated for user: {}", userId);

        return ResponseEntity.ok(new AccountDeletionResponse(
                "Account has been successfully deactivated. Your data will be retained for 30 days before permanent deletion.",
                "deactivated",
                Instant.now()
        ));
    }
}
