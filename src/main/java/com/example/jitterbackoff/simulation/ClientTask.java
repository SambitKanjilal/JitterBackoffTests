package com.example.jitterbackoff.simulation;

import com.example.jitterbackoff.backoff.BackoffStrategy;
import com.example.jitterbackoff.service.CounterService;

import java.util.concurrent.Callable;

public class ClientTask implements Callable<ClientResult> {

    private final int clientId;
    private final BackoffStrategy strategy;
    private final SimulationConfig config;
    private final CounterService counterService;

    public ClientTask(int clientId, BackoffStrategy strategy,
                      SimulationConfig config, CounterService counterService) {
        this.clientId = clientId;
        this.strategy = strategy;
        this.config = config;
        this.counterService = counterService;
    }

    @Override
    public ClientResult call() {
        long start = System.currentTimeMillis();
        int attempts = 0;
        long lastSleep = 0;

        for (int attempt = 0; attempt <= config.maxAttempts(); attempt++) {
            attempts++;
            if (counterService.tryIncrement()) {
                return new ClientResult(clientId, attempts, true,
                        System.currentTimeMillis() - start);
            }
            long sleep = strategy.computeSleepMs(attempt, lastSleep,
                    config.baseDelayMs(), config.capMs());
            lastSleep = sleep;
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return new ClientResult(clientId, attempts, false,
                System.currentTimeMillis() - start);
    }
}
