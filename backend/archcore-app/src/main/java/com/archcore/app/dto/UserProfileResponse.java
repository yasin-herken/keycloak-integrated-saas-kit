package com.archcore.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserProfileResponse(
    String userId,
    String firstName,
    String lastName,
    String profilePictureUrl,
    String fullName,
    boolean accountActive,
    Instant createdAt,
    Instant updatedAt
) {}
