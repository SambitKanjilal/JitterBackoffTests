package com.example.jitterbackoff.simulation;

import com.example.jitterbackoff.backoff.BackoffStrategy;
import com.example.jitterbackoff.service.CounterService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

@Component
public class SimulationEngine {

    private final CounterService counterService;

    public SimulationEngine(CounterService counterService) {
        this.counterService = counterService;
    }

    /**
     * Runs one strategy against a freshly reset counter.
     * All numClients tasks are submitted simultaneously so they race from the start.
     */
    public SimulationResult run(BackoffStrategy strategy, SimulationConfig config) {
        counterService.resetCounter();

        List<ClientTask> tasks = IntStream.range(0, config.numClients())
                .mapToObj(i -> new ClientTask(i, strategy, config, counterService))
                .toList();

        long wallStart = System.currentTimeMillis();

        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
        List<ClientResult> results;
        try {
            List<Future<ClientResult>> futures = pool.invokeAll(tasks);
            results = futures.stream()
                    .map(f -> {
                        try { return f.get(); }
                        catch (Exception e) { throw new RuntimeException(e); }
                    })
                    .toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Simulation interrupted", e);
        } finally {
            pool.shutdown();
        }

        long totalWall      = System.currentTimeMillis() - wallStart;
        long totalCalls     = results.stream().mapToLong(ClientResult::totalAttempts).sum();
        long succeeded      = results.stream().filter(ClientResult::succeeded).count();
        double avgAttempts  = results.stream().mapToInt(ClientResult::totalAttempts).average().orElse(0);

        return new SimulationResult(
                strategy.name(),
                totalCalls,
                totalCalls - succeeded,
                succeeded,
                totalWall,
                avgAttempts
        );
    }
}
