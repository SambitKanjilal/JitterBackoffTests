package com.example.jitterbackoff.backoff;

public interface BackoffStrategy {

    String name();

    /**
     * @param attempt     zero-based retry index (0 = first retry after initial failure)
     * @param lastSleepMs sleep used on the previous attempt; 0 on attempt 0
     * @param baseMs      configured base delay in milliseconds
     * @param capMs       configured maximum sleep in milliseconds
     * @return milliseconds to sleep before the next attempt
     */
    long computeSleepMs(int attempt, long lastSleepMs, long baseMs, long capMs);
}
