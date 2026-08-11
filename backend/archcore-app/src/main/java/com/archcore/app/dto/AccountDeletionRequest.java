package com.archcore.app.dto;

import jakarta.validation.constraints.Size;

public record AccountDeletionRequest(
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    String reason,

    boolean confirmDeletion
) {}
