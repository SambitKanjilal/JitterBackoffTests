package com.example.jitterbackoff.backoff;

import org.junit.jupiter.api.Test;

import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

class EqualJitterStrategyTest {

    private static final int ITERATIONS = 10_000;
    private final EqualJitterStrategy strategy = new EqualJitterStrategy();

    @Test
    void allValuesAboveFloor() {
        long ceiling = Math.min(400, 20 * (1L << 3)); // attempt=3: 160
        long floor = ceiling / 2;                      // 80
        LongStream.range(0, ITERATIONS)
                .map(i -> strategy.computeSleepMs(3, 0, 20, 400))
                .forEach(v -> assertThat(v).isGreaterThanOrEqualTo(floor));
    }

    @Test
    void allValuesBelowOrEqualCeiling() {
        long ceiling = Math.min(400, 20 * (1L << 3)); // 160
        LongStream.range(0, ITERATIONS)
                .map(i -> strategy.computeSleepMs(3, 0, 20, 400))
                .forEach(v -> assertThat(v).isLessThanOrEqualTo(ceiling));
    }

    @Test
    void neverReturnsZero() {
        long zeros = LongStream.range(0, ITERATIONS)
                .map(i -> strategy.computeSleepMs(3, 0, 20, 400))
                .filter(v -> v == 0)
                .count();
        assertThat(zeros).isZero();
    }

    @Test
    void meanApproximatelyThreeQuartersCeiling() {
        long ceiling = Math.min(400, 20 * (1L << 3)); // 160; expected mean = 120
        double mean = LongStream.range(0, ITERATIONS)
                .map(i -> strategy.computeSleepMs(3, 0, 20, 400))
                .average().orElseThrow();
        double expected = ceiling * 0.75;
        assertThat(mean).isBetween(expected * 0.90, expected * 1.10);
    }

    @Test
    void capEnforced() {
        LongStream.range(0, ITERATIONS)
                .map(i -> strategy.computeSleepMs(30, 0, 20, 400))
                .forEach(v -> assertThat(v).isBetween(200L, 400L));
    }
}
