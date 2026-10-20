package com.example.jitterbackoff.backoff;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoJitterStrategyTest {

    private final NoJitterStrategy strategy = new NoJitterStrategy();

    @Test
    void attempt0_returnsBase() {
        assertThat(strategy.computeSleepMs(0, 0, 20, 400)).isEqualTo(20);
    }

    @Test
    void attempt1_doublesBase() {
        assertThat(strategy.computeSleepMs(1, 0, 20, 400)).isEqualTo(40);
    }

    @Test
    void attempt4_returns320() {
        assertThat(strategy.computeSleepMs(4, 0, 20, 400)).isEqualTo(320);
    }

    @Test
    void attempt5_isCapped() {
        assertThat(strategy.computeSleepMs(5, 0, 20, 400)).isEqualTo(400);
    }

    @Test
    void highAttempt_neverExceedsCap() {
        assertThat(strategy.computeSleepMs(30, 0, 20, 400)).isEqualTo(400);
        assertThat(strategy.computeSleepMs(63, 0, 20, 400)).isEqualTo(400);
    }

    @Test
    void lastSleepMs_isIgnored() {
        assertThat(strategy.computeSleepMs(2, 9999, 20, 400))
                .isEqualTo(strategy.computeSleepMs(2, 0, 20, 400));
    }
}
