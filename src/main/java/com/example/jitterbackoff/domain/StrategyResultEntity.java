package com.example.jitterbackoff.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "strategy_result")
public class StrategyResultEntity {

    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_run_id", nullable = false)
    private SimulationRunEntity simulationRun;
    @Column(nullable = false, length = 30) private String strategyName;
    @Column(nullable = false) private long totalCalls;
    @Column(nullable = false) private long wastedCalls;
    @Column(nullable = false) private long succeededClients;
    @Column(nullable = false) private long totalDurationMs;
    @Column(nullable = false) private double avgAttemptsPerClient;

    public StrategyResultEntity() {}

    public UUID getId()                                             { return id; }
    public void setId(UUID id)                                      { this.id = id; }
    public SimulationRunEntity getSimulationRun()                   { return simulationRun; }
    public void setSimulationRun(SimulationRunEntity v)             { this.simulationRun = v; }
    public String getStrategyName()                                 { return strategyName; }
    public void setStrategyName(String v)                           { this.strategyName = v; }
    public long getTotalCalls()                                     { return totalCalls; }
    public void setTotalCalls(long v)                               { this.totalCalls = v; }
    public long getWastedCalls()                                    { return wastedCalls; }
    public void setWastedCalls(long v)                              { this.wastedCalls = v; }
    public long getSucceededClients()                               { return succeededClients; }
    public void setSucceededClients(long v)                         { this.succeededClients = v; }
    public long getTotalDurationMs()                                { return totalDurationMs; }
    public void setTotalDurationMs(long v)                          { this.totalDurationMs = v; }
    public double getAvgAttemptsPerClient()                         { return avgAttemptsPerClient; }
    public void setAvgAttemptsPerClient(double v)                   { this.avgAttemptsPerClient = v; }
}
