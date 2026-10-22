package com.example.jitterbackoff.web.dto;

public record StrategyResultDto(
        String strategyName,
        long totalCalls,
        long wastedCalls,
        long succeededClients,
        long totalDurationMs,
        double avgAttemptsPerClient,
        double goodput,                    // succeededClients / numClients (0.0–1.0)
        Double wastedCallsVsNoJitterPct,   // null for NO_JITTER; negative = improvement
        Double wallTimeVsNoJitterPct
) {}
