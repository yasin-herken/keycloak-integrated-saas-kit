package com.archcore.app.controller;

import com.archcore.app.dto.UserRegistrationRequest;
import com.archcore.app.dto.UserRegistrationResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.archcore.app.util.LogMaskingUtility;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class UserRegistrationController {

    private static final Logger log = LoggerFactory.getLogger(UserRegistrationController.class);

    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponse> register(
            @Valid @RequestBody UserRegistrationRequest request) {

        log.info("Registration request for user: {}, email: {}",
            LogMaskingUtility.mask(request.username()),
            LogMaskingUtility.mask(request.email()));

        String userId = UUID.randomUUID().toString();

        UserRegistrationResponse response = new UserRegistrationResponse(
            userId,
            request.username(),
            request.email(),
            Instant.now()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
