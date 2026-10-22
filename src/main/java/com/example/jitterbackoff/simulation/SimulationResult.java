package com.example.jitterbackoff.simulation;

public record SimulationResult(
        String strategyName,
        long totalCalls,
        long wastedCalls,
        long succeededClients,
        long totalWallTimeMs,
        double avgAttemptsPerClient,
        double goodput         // succeededClients / numClients expressed as 0.0–1.0
) {}
