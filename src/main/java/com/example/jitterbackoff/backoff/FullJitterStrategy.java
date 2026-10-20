package com.example.jitterbackoff.backoff;

import java.util.concurrent.ThreadLocalRandom;

public class FullJitterStrategy implements BackoffStrategy {

    @Override
    public String name() {
        return "FULL_JITTER";
    }

    @Override
    public long computeSleepMs(int attempt, long lastSleepMs, long baseMs, long capMs) {
        if (attempt >= 30) return ThreadLocalRandom.current().nextLong(0, capMs + 1);
        long ceiling = Math.min(capMs, baseMs * (1L << attempt));
        return ThreadLocalRandom.current().nextLong(0, ceiling + 1);
    }
}
