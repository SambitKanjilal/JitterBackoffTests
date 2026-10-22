package com.example.jitterbackoff.web.dto;

public record SimulationConfigDto(int numClients, long baseDelayMs, long capMs, int maxAttempts) {}
