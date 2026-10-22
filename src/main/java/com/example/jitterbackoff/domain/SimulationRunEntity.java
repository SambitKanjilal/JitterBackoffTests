package com.example.jitterbackoff.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "simulation_run")
public class SimulationRunEntity {

    @Id private UUID id;
    @Column(nullable = false) private Instant startedAt;
    private Instant completedAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private RunStatus status;
    @Column(nullable = false) private int numClients;
    @Column(nullable = false) private long baseDelayMs;
    @Column(nullable = false) private long capMs;
    @Column(nullable = false) private int maxAttempts;

    @OneToMany(mappedBy = "simulationRun", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StrategyResultEntity> strategyResults = new ArrayList<>();

    public SimulationRunEntity() {}

    public UUID getId()                                    { return id; }
    public void setId(UUID id)                             { this.id = id; }
    public Instant getStartedAt()                          { return startedAt; }
    public void setStartedAt(Instant v)                    { this.startedAt = v; }
    public Instant getCompletedAt()                        { return completedAt; }
    public void setCompletedAt(Instant v)                  { this.completedAt = v; }
    public RunStatus getStatus()                           { return status; }
    public void setStatus(RunStatus v)                     { this.status = v; }
    public int getNumClients()                             { return numClients; }
    public void setNumClients(int v)                       { this.numClients = v; }
    public long getBaseDelayMs()                           { return baseDelayMs; }
    public void setBaseDelayMs(long v)                     { this.baseDelayMs = v; }
    public long getCapMs()                                 { return capMs; }
    public void setCapMs(long v)                           { this.capMs = v; }
    public int getMaxAttempts()                            { return maxAttempts; }
    public void setMaxAttempts(int v)                      { this.maxAttempts = v; }
    public List<StrategyResultEntity> getStrategyResults() { return strategyResults; }
}
