package com.example.jitterbackoff.service;

import com.example.jitterbackoff.chaos.ChaosState;
import com.example.jitterbackoff.domain.CounterEntity;
import com.example.jitterbackoff.repository.CounterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CounterService {

    private final CounterRepository repo;
    private final ChaosState chaosState;

    public CounterService(CounterRepository repo, ChaosState chaosState) {
        this.repo = repo;
        this.chaosState = chaosState;
    }

    /**
     * Attempts one optimistic-concurrency increment.
     *
     * REQUIRES_NEW — each call is its own committed transaction. Without this,
     * Hibernate's first-level cache causes every client to read its own stale
     * snapshot: no contention is ever observed.
     *
     * @return true if this client won the race, false if another client did
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryIncrement() {
        chaosState.maybeInjectLatency();   // no-op unless a spike is active
        CounterEntity counter = repo.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Counter row missing"));
        return repo.incrementIfVersionMatches(1L, counter.getVersion()) == 1;
    }

    /** Resets the counter to 0 before each strategy's run. REQUIRES_NEW commits immediately. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetCounter() {
        CounterEntity counter = repo.findById(1L)
                .orElseGet(() -> new CounterEntity(1L, 0L, 0L));
        counter.setValue(0L);
        repo.saveAndFlush(counter);
    }

    @Transactional(readOnly = true)
    public long currentValue() {
        return repo.findById(1L).map(CounterEntity::getValue).orElse(0L);
    }
}
