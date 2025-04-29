CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    time_zone VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TYPE morning_feeling AS ENUM ('BAD', 'OK', 'GOOD');

CREATE TABLE sleep_logs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    sleep_date DATE NOT NULL,
    local_time_to_bed TIME NOT NULL,
    local_time_out_of_bed TIME NOT NULL,
    utc_time_to_bed TIMESTAMPTZ NOT NULL,
    utc_time_out_of_bed TIMESTAMPTZ NOT NULL,
    time_zone_id VARCHAR(50) NOT NULL,
    total_time_in_bed_minutes INTEGER NOT NULL,
    feeling morning_feeling NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT unique_user_sleep_date UNIQUE (user_id, sleep_date),
    CONSTRAINT valid_utc_sleep_interval CHECK (utc_time_out_of_bed > utc_time_to_bed)
);

CREATE INDEX idx_sleep_logs_user_id ON sleep_logs(user_id);
CREATE INDEX idx_sleep_logs_sleep_date ON sleep_logs(sleep_date);
CREATE INDEX idx_sleep_logs_user_date ON sleep_logs(user_id, sleep_date DESC);
