package com.example.jitterbackoff.service;

import com.example.jitterbackoff.domain.CounterEntity;
import com.example.jitterbackoff.repository.CounterRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CounterService {

    private final CounterRepository repo;

    public CounterService(CounterRepository repo) {
        this.repo = repo;
    }

    /**
     * Attempts a single optimistic-lock increment of the shared counter row.
     *
     * REQUIRES_NEW: each call is its own committed transaction. Without this,
     * Hibernate's session cache would cause every client to read its own stale
     * snapshot — no OCC contention would ever be observed.
     *
     * saveAndFlush: forces the UPDATE (and its version predicate) to execute
     * immediately inside this transaction so the exception surfaces here where
     * it can be caught, rather than deferring to an outer commit boundary.
     *
     * @return true if the increment succeeded, false if the optimistic lock was lost
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryIncrement() {
        try {
            CounterEntity counter = repo.findById(1L)
                    .orElseThrow(() -> new IllegalStateException("Counter row missing — was resetCounter() called?"));
            counter.setValue(counter.getValue() + 1);
            repo.saveAndFlush(counter);
            return true;
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
            return false;
        }
    }

    /**
     * Resets the counter value to 0 before each strategy's simulation run.
     * Also REQUIRES_NEW so it commits immediately and all client threads see
     * the clean baseline before the thread pool starts.
     *
     * Does NOT manually reset the version column — JPA manages it, and the
     * reset write itself increments it, which is fine: all clients start from
     * a single consistent committed baseline regardless of the version number.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetCounter() {
        CounterEntity counter = repo.findById(1L)
                .orElseGet(() -> new CounterEntity(1L, 0L, 0L));
        counter.setValue(0L);
        repo.saveAndFlush(counter);
    }

    /** Returns the current committed counter value. Read-only, no new transaction needed. */
    @Transactional(readOnly = true)
    public long currentValue() {
        return repo.findById(1L)
                .map(CounterEntity::getValue)
                .orElse(0L);
    }
}
