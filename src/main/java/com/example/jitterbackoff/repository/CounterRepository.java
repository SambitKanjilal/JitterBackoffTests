package com.example.jitterbackoff.repository;

import com.example.jitterbackoff.domain.CounterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CounterRepository extends JpaRepository<CounterEntity, Long> {

    /**
     * Atomically increments value and bumps version only when version still matches.
     * Returns 1 on success, 0 if another client already won the race.
     * No exception on conflict — the transaction always commits cleanly.
     * (JPA @Version throws StaleObjectStateException which marks the session
     * rollback-only even when caught, causing UnexpectedRollbackException.)
     */
    @Modifying
    @Query("UPDATE CounterEntity c SET c.value = c.value + 1, c.version = c.version + 1 " +
           "WHERE c.id = :id AND c.version = :version")
    int incrementIfVersionMatches(@Param("id") Long id, @Param("version") Long version);
}
