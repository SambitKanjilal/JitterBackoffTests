package com.example.jitterbackoff.repository;

import com.example.jitterbackoff.domain.RunStatus;
import com.example.jitterbackoff.domain.SimulationRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SimulationRunRepository extends JpaRepository<SimulationRunEntity, UUID> {
    List<SimulationRunEntity> findAllByOrderByStartedAtDesc();
    List<SimulationRunEntity> findByStatusOrderByStartedAtDesc(RunStatus status);
}
