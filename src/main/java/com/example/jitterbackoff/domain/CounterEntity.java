package com.example.jitterbackoff.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "counter")
public class CounterEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long value;

    @Version
    private Long version;

    public CounterEntity() {}

    public CounterEntity(Long id, Long value, Long version) {
        this.id = id;
        this.value = value;
        this.version = version;
    }

    public Long getId()               { return id; }
    public Long getValue()            { return value; }
    public void setValue(Long value)  { this.value = value; }
    public Long getVersion()          { return version; }
}
