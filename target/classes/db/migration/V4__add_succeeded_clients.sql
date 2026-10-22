ALTER TABLE strategy_result
    ADD COLUMN succeeded_clients BIGINT NOT NULL DEFAULT 0;
