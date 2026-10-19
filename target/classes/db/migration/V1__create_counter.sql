CREATE TABLE counter (
    id      BIGINT PRIMARY KEY,
    value   BIGINT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0
);

INSERT INTO counter(id, value, version) VALUES (1, 0, 0);
