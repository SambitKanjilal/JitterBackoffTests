package com.example.jitterbackoff.simulation;

public record ClientResult(
        int clientId,
        int totalAttempts,
        boolean succeeded,
        long wallTimeMs
) {}
