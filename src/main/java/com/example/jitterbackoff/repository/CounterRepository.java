package com.example.jitterbackoff.repository;

import com.example.jitterbackoff.domain.CounterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounterRepository extends JpaRepository<CounterEntity, Long> {
}
