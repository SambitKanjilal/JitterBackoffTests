package com.example.jitterbackoff.web.dto;

import java.time.Instant;
import java.util.UUID;

public record SimulationStatusResponse(
        UUID runId,
        String status,
        Instant startedAt,
        Instant completedAt,
        SimulationConfigDto config,
        String pollUrl,
        String resultsUrl
) {}
