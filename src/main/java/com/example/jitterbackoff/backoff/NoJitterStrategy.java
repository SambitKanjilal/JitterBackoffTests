package com.example.jitterbackoff.backoff;

public class NoJitterStrategy implements BackoffStrategy {

    @Override
    public String name() {
        return "NO_JITTER";
    }

    @Override
    public long computeSleepMs(int attempt, long lastSleepMs, long baseMs, long capMs) {
        if (attempt >= 30) return capMs;
        return Math.min(capMs, baseMs * (1L << attempt));
    }
}
