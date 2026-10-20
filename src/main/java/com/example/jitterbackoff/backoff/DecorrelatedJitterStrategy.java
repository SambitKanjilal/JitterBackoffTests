package com.example.jitterbackoff.backoff;

import java.util.concurrent.ThreadLocalRandom;

public class DecorrelatedJitterStrategy implements BackoffStrategy {

    @Override
    public String name() {
        return "DECORRELATED_JITTER";
    }

    @Override
    public long computeSleepMs(int attempt, long lastSleepMs, long baseMs, long capMs) {
        long lower = baseMs;
        long upper = Math.max(baseMs + 1, lastSleepMs * 3);
        long raw = ThreadLocalRandom.current().nextLong(lower, upper + 1);
        return Math.min(capMs, raw);
    }
}
