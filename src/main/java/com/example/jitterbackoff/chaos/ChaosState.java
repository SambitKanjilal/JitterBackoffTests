package com.example.jitterbackoff.chaos;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe singleton that injects artificial latency into CounterService.tryIncrement().
 *
 * When active, every tryIncrement call sleeps for spikeMs before attempting the DB write.
 * This simulates a downstream latency spike (e.g. a slow DynamoDB partition, a GC pause,
 * or a network blip) and causes retry storms: clients time out their retries while the
 * resource is still unavailable, keeping goodput well below 100%.
 */
@Component
public class ChaosState {

    private final AtomicBoolean active  = new AtomicBoolean(false);
    private final AtomicLong    spikeMs = new AtomicLong(0);

    public void activate(long latencyMs) {
        spikeMs.set(latencyMs);
        active.set(true);
    }

    public void deactivate() {
        active.set(false);
        spikeMs.set(0);
    }

    public boolean isActive()    { return active.get(); }
    public long    getSpikeMs()  { return spikeMs.get(); }

    /**
     * Called at the start of every tryIncrement. If a spike is active, sleeps for
     * the configured duration before the DB write proceeds.
     */
    public void maybeInjectLatency() {
        if (!active.get()) return;
        try {
            Thread.sleep(spikeMs.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
