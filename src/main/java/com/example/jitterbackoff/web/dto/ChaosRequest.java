package com.example.jitterbackoff.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ChaosRequest(
        @Min(0) @Max(60000) long latencyMs   // 0 = deactivate; 1–60000 = inject this many ms
) {}
