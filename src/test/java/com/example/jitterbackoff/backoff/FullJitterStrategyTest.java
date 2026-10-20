package com.example.jitterbackoff.backoff;

import org.junit.jupiter.api.Test;

import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

class FullJitterStrategyTest {

    private static final int ITERATIONS = 10_000;
    private final FullJitterStrategy strategy = new FullJitterStrategy();

    @Test
    void allValuesWithinBounds() {
        long ceiling = Math.min(400, 20 * (1L << 3)); // attempt=3: 160
        LongStream.range(0, ITERATIONS)
                .map(i -> strategy.computeSleepMs(3, 0, 20, 400))
                .forEach(v -> assertThat(v).isBetween(0L, ceiling));
    }

    @Test
    void meanApproximatelyHalfCeiling() {
        long ceiling = Math.min(400, 20 * (1L << 3)); // 160
        double mean = LongStream.range(0, ITERATIONS)
                .map(i -> strategy.computeSleepMs(3, 0, 20, 400))
                .average().orElseThrow();
        assertThat(mean).isBetween(ceiling * 0.40, ceiling * 0.60);
    }

    @Test
    void capEnforced() {
        LongStream.range(0, ITERATIONS)
                .map(i -> strategy.computeSleepMs(30, 0, 20, 400))
                .forEach(v -> assertThat(v).isBetween(0L, 400L));
    }

    @Test
    void producesZeroValues() {
        long zeros = LongStream.range(0, ITERATIONS)
                .map(i -> strategy.computeSleepMs(1, 0, 20, 400))
                .filter(v -> v == 0)
                .count();
        assertThat(zeros).isGreaterThan(0);
    }
}
