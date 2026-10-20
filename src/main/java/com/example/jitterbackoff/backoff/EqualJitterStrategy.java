package com.example.jitterbackoff.backoff;

import java.util.concurrent.ThreadLocalRandom;

public class EqualJitterStrategy implements BackoffStrategy {

    @Override
    public String name() {
        return "EQUAL_JITTER";
    }

    @Override
    public long computeSleepMs(int attempt, long lastSleepMs, long baseMs, long capMs) {
        if (attempt >= 30) {
            long half = capMs / 2;
            return half + ThreadLocalRandom.current().nextLong(0, half + 1);
        }
        long ceiling = Math.min(capMs, baseMs * (1L << attempt));
        long half = ceiling / 2;
        return half + ThreadLocalRandom.current().nextLong(0, half + 1);
    }
}
