CREATE TABLE strategy_result (
    id                      UUID PRIMARY KEY,
    simulation_run_id       UUID         NOT NULL REFERENCES simulation_run(id),
    strategy_name           VARCHAR(30)  NOT NULL,
    total_calls             BIGINT       NOT NULL,
    wasted_calls            BIGINT       NOT NULL,
    total_duration_ms       BIGINT       NOT NULL,
    avg_attempts_per_client NUMERIC(6,2) NOT NULL
);
