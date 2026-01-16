CREATE TABLE IF NOT EXISTS devices (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    brand TEXT NOT NULL,
    state VARCHAR(20) NOT NULL,
    creation_time TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT,
    CONSTRAINT state_check CHECK (state IN ('AVAILABLE','IN_USE','INACTIVE'))
);