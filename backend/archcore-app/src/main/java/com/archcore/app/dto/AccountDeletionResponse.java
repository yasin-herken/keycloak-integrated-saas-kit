package com.archcore.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AccountDeletionResponse(
    String message,
    String status,
    Instant processedAt
) {}
