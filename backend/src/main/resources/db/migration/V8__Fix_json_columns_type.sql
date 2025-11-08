-- Update invocations table to use JSONB instead of JSON for better PostgreSQL compatibility
-- This fixes the type casting issue when inserting JSON data

-- Convert JSON columns to JSONB
ALTER TABLE invocations ALTER COLUMN solution_ids TYPE JSONB USING solution_ids::JSONB;
ALTER TABLE invocations ALTER COLUMN test_ids TYPE JSONB USING test_ids::JSONB;
ALTER TABLE invocations ALTER COLUMN results TYPE JSONB USING results::JSONB;