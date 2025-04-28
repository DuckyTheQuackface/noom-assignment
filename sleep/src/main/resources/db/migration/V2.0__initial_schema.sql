-- Create users table
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       time_zone VARCHAR(100) NOT NULL DEFAULT 'UTC',
                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Create feeling enum type for morning feeling
CREATE TYPE morning_feeling AS ENUM ('BAD', 'OK', 'GOOD');

-- Create sleep_logs table
CREATE TABLE sleep_logs (
                            id SERIAL PRIMARY KEY,
                            user_id INTEGER NOT NULL REFERENCES users(id),
                            sleep_date DATE NOT NULL,
                            time_to_bed TIMESTAMPTZ NOT NULL,
                            time_out_of_bed TIMESTAMPTZ NOT NULL,
                            total_time_in_bed INTEGER NOT NULL, -- Duration in minutes
                            feeling morning_feeling NOT NULL,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Each user can only have one sleep log for a given date
                            CONSTRAINT unique_user_sleep_date UNIQUE (user_id, sleep_date),

    -- Ensure time_out_of_bed is after time_to_bed
                            CONSTRAINT valid_sleep_interval CHECK (time_out_of_bed > time_to_bed)
);

-- Create indexes for common queries
CREATE INDEX idx_sleep_logs_user_id ON sleep_logs(user_id);
CREATE INDEX idx_sleep_logs_sleep_date ON sleep_logs(sleep_date);
CREATE INDEX idx_sleep_logs_user_date ON sleep_logs(user_id, sleep_date DESC);