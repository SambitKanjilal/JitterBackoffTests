package com.example.jitterbackoff.web.dto;

public record ChaosStatusResponse(
        boolean active,
        long latencyMs,
        String description
) {}
