package com.example.jitterbackoff.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SimulationRequest(
        @Min(1) @Max(50) int numClients,
        @Min(1) long baseDelayMs,
        @Min(1) long capMs,
        @Min(1) int maxAttempts
) {}
