package com.archcore.app.dto;

import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    String firstName,

    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    String lastName,

    @Size(max = 500, message = "Profile picture URL must not exceed 500 characters")
    String profilePictureUrl
) {}
