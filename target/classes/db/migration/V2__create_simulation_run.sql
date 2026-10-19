CREATE TABLE simulation_run (
    id            UUID PRIMARY KEY,
    started_at    TIMESTAMPTZ NOT NULL,
    completed_at  TIMESTAMPTZ,
    status        VARCHAR(20)  NOT NULL DEFAULT 'RUNNING',
    num_clients   INT          NOT NULL,
    base_delay_ms INT          NOT NULL,
    cap_ms        INT          NOT NULL,
    max_attempts  INT          NOT NULL
);
