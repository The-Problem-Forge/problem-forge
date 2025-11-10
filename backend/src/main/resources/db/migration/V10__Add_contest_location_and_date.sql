-- Add location and date fields to contests table
ALTER TABLE contests ADD COLUMN location VARCHAR(255);
ALTER TABLE contests ADD COLUMN contest_date TIMESTAMP WITHOUT TIME ZONE;