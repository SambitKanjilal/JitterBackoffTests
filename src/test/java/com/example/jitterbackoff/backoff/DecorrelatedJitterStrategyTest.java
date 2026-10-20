package com.example.jitterbackoff.backoff;

import org.junit.jupiter.api.Test;

import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

class DecorrelatedJitterStrategyTest {

    private static final int ITERATIONS = 10_000;
    private final DecorrelatedJitterStrategy strategy = new DecorrelatedJitterStrategy();

    @Test
    void floorIsBaseWhenLastSleepIsZero() {
        LongStream.range(0, ITERATIONS)
                .map(i -> strategy.computeSleepMs(0, 0, 20, 400))
                .forEach(v -> assertThat(v).isGreaterThanOrEqualTo(20L));
    }

    @Test
    void upperBoundIsThreeTimesLastSleep() {
        long lastSleep = 100;
        long expectedUpper = Math.min(400, lastSleep * 3); // 300, under cap
        LongStream.range(0, ITERATIONS)
                .map(i -> strategy.computeSleepMs(1, lastSleep, 20, 400))
                .forEach(v -> {
                    assertThat(v).isGreaterThanOrEqualTo(20L);
                    assertThat(v).isLessThanOrEqualTo(expectedUpper);
                });
    }

    @Test
    void capEnforced() {
        LongStream.range(0, ITERATIONS)
                .map(i -> strategy.computeSleepMs(5, 10_000, 20, 400))
                .forEach(v -> assertThat(v).isLessThanOrEqualTo(400L));
    }

    @Test
    void largeCap_upperBoundDrivenByLastSleep() {
        long lastSleep = 50;
        long cap = 10_000;
        long expectedUpper = lastSleep * 3; // 150
        LongStream.range(0, ITERATIONS)
                .map(i -> strategy.computeSleepMs(1, lastSleep, 20, cap))
                .forEach(v -> {
                    assertThat(v).isGreaterThanOrEqualTo(20L);
                    assertThat(v).isLessThanOrEqualTo(expectedUpper);
                });
    }

    @Test
    void lastSleepZeroWithNonZeroAttempt_floorIsBase() {
        LongStream.range(0, ITERATIONS)
                .map(i -> strategy.computeSleepMs(3, 0, 20, 400))
                .forEach(v -> assertThat(v).isGreaterThanOrEqualTo(20L));
    }
}
