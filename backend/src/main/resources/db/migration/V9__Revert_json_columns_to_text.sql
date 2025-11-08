-- Convert JSONB columns back to TEXT to avoid PostgreSQL type casting issues
-- The JSON conversion will be handled by the application layer

-- Convert JSONB columns back to TEXT
ALTER TABLE invocations ALTER COLUMN solution_ids TYPE TEXT USING solution_ids::TEXT;
ALTER TABLE invocations ALTER COLUMN test_ids TYPE TEXT USING test_ids::TEXT;
ALTER TABLE invocations ALTER COLUMN results TYPE TEXT USING results::TEXT;