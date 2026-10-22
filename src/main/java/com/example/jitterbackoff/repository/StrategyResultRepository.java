package com.example.jitterbackoff.repository;

import com.example.jitterbackoff.domain.StrategyResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StrategyResultRepository extends JpaRepository<StrategyResultEntity, UUID> {
    List<StrategyResultEntity> findBySimulationRunIdOrderByStrategyName(UUID simulationRunId);
}
