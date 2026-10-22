package com.example.jitterbackoff.simulation;

public record SimulationConfig(
        int numClients,
        long baseDelayMs,
        long capMs,
        int maxAttempts
) {}
