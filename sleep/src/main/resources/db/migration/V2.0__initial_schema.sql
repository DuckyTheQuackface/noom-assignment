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
                            local_time_to_bed TIME NOT NULL,     -- User's local time
                            local_time_out_of_bed TIME NOT NULL, -- User's local time
                            utc_time_to_bed TIMESTAMPTZ NOT NULL,     -- UTC time
                            utc_time_out_of_bed TIMESTAMPTZ NOT NULL, -- UTC time
                            time_zone_id VARCHAR(50) NOT NULL,
                            total_time_in_bed INTEGER NOT NULL,  -- Duration in minutes
                            feeling morning_feeling NOT NULL,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                            CONSTRAINT unique_user_sleep_date UNIQUE (user_id, sleep_date),
                            CONSTRAINT valid_utc_sleep_interval CHECK (utc_time_out_of_bed > utc_time_to_bed)
);

-- Create indexes for common queries
CREATE INDEX idx_sleep_logs_user_id ON sleep_logs(user_id);
CREATE INDEX idx_sleep_logs_sleep_date ON sleep_logs(sleep_date);
CREATE INDEX idx_sleep_logs_user_date ON sleep_logs(user_id, sleep_date DESC);
