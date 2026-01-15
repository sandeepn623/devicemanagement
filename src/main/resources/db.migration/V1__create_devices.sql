-- Enable pgcrypto for gen_random_uuid (PostgreSQL 13+)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    brand TEXT NOT NULL,
    state VARCHAR(20) NOT NULL,
    creation_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT state_check CHECK (state IN ('AVAILABLE','IN_USE','INACTIVE'))
);