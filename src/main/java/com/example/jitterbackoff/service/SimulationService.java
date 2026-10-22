package com.example.jitterbackoff.service;

import com.example.jitterbackoff.backoff.BackoffStrategy;
import com.example.jitterbackoff.domain.RunStatus;
import com.example.jitterbackoff.domain.SimulationRunEntity;
import com.example.jitterbackoff.domain.StrategyResultEntity;
import com.example.jitterbackoff.repository.SimulationRunRepository;
import com.example.jitterbackoff.repository.StrategyResultRepository;
import com.example.jitterbackoff.simulation.SimulationConfig;
import com.example.jitterbackoff.simulation.SimulationEngine;
import com.example.jitterbackoff.simulation.SimulationResult;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class SimulationService {

    private static final Logger log = LoggerFactory.getLogger(SimulationService.class);

    private final SimulationEngine engine;
    private final List<BackoffStrategy> orderedStrategies;
    private final SimulationRunRepository runRepo;
    private final StrategyResultRepository resultRepo;

    private final ExecutorService asyncPool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "simulation-runner");
        t.setDaemon(true);
        return t;
    });

    public SimulationService(SimulationEngine engine,
                             List<BackoffStrategy> orderedStrategies,
                             SimulationRunRepository runRepo,
                             StrategyResultRepository resultRepo) {
        this.engine = engine;
        this.orderedStrategies = orderedStrategies;
        this.runRepo = runRepo;
        this.resultRepo = resultRepo;
    }

    @Transactional
    public UUID startRun(SimulationConfig config) {
        SimulationRunEntity run = buildRun(config);
        runRepo.save(run);
        asyncPool.submit(() -> executeRun(run.getId(), config, orderedStrategies));
        return run.getId();
    }

    @Transactional
    public UUID startSingleStrategyRun(String strategyName, SimulationConfig config) {
        BackoffStrategy strategy = orderedStrategies.stream()
                .filter(s -> s.name().equalsIgnoreCase(strategyName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown strategy: " + strategyName));
        SimulationRunEntity run = buildRun(config);
        runRepo.save(run);
        asyncPool.submit(() -> executeRun(run.getId(), config, List.of(strategy)));
        return run.getId();
    }

    @Transactional(readOnly = true)
    public SimulationRunEntity getRunStatus(UUID runId) {
        return runRepo.findById(runId)
                .orElseThrow(() -> new NoSuchElementException("Run not found: " + runId));
    }

    @Transactional(readOnly = true)
    public List<StrategyResultEntity> getResults(UUID runId) {
        if (!runRepo.existsById(runId)) throw new NoSuchElementException("Run not found: " + runId);
        return resultRepo.findBySimulationRunIdOrderByStrategyName(runId);
    }

    @Transactional(readOnly = true)
    public List<SimulationRunEntity> listAllRuns() {
        return runRepo.findAllByOrderByStartedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<SimulationRunEntity> listCompletedRuns() {
        return runRepo.findByStatusOrderByStartedAtDesc(RunStatus.DONE);
    }

    private void executeRun(UUID runId, SimulationConfig config, List<BackoffStrategy> strategies) {
        try {
            for (BackoffStrategy strategy : strategies) {
                log.info("strategy={} run={}", strategy.name(), runId);
                SimulationResult r = engine.run(strategy, config);
                persistResult(runId, r);
                log.info("strategy={} totalCalls={} wasted={} goodput={:.2f} wall={}ms",
                        r.strategyName(), r.totalCalls(), r.wastedCalls(), r.goodput(), r.totalWallTimeMs());
            }
            markDone(runId);
        } catch (Exception e) {
            log.error("Run {} failed", runId, e);
            markFailed(runId);
        }
    }

    @Transactional
    protected void persistResult(UUID runId, SimulationResult r) {
        StrategyResultEntity e = new StrategyResultEntity();
        e.setId(UUID.randomUUID());
        e.setSimulationRun(runRepo.getReferenceById(runId));
        e.setStrategyName(r.strategyName());
        e.setTotalCalls(r.totalCalls());
        e.setWastedCalls(r.wastedCalls());
        e.setSucceededClients(r.succeededClients());
        e.setTotalDurationMs(r.totalWallTimeMs());
        e.setAvgAttemptsPerClient(r.avgAttemptsPerClient());
        resultRepo.save(e);
    }

    @Transactional
    protected void markDone(UUID runId) {
        SimulationRunEntity run = runRepo.findById(runId).orElseThrow();
        run.setStatus(RunStatus.DONE);
        run.setCompletedAt(Instant.now());
        runRepo.save(run);
    }

    @Transactional
    protected void markFailed(UUID runId) {
        SimulationRunEntity run = runRepo.findById(runId).orElseThrow();
        run.setStatus(RunStatus.FAILED);
        run.setCompletedAt(Instant.now());
        runRepo.save(run);
    }

    private SimulationRunEntity buildRun(SimulationConfig config) {
        SimulationRunEntity run = new SimulationRunEntity();
        run.setId(UUID.randomUUID());
        run.setStartedAt(Instant.now());
        run.setStatus(RunStatus.RUNNING);
        run.setNumClients(config.numClients());
        run.setBaseDelayMs(config.baseDelayMs());
        run.setCapMs(config.capMs());
        run.setMaxAttempts(config.maxAttempts());
        return run;
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        asyncPool.shutdown();
        if (!asyncPool.awaitTermination(30, TimeUnit.SECONDS)) asyncPool.shutdownNow();
    }
}
