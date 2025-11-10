-- Add checker tests table

CREATE TABLE checker_tests (
    test_id BIGSERIAL NOT NULL,
    problem_id BIGINT NOT NULL,
    input TEXT NOT NULL,
    output TEXT NOT NULL,
    expected TEXT NOT NULL,
    verdict VARCHAR(50) NOT NULL DEFAULT 'OK',
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    PRIMARY KEY (test_id),
    FOREIGN KEY (problem_id) REFERENCES problems (problem_id)
);

-- Update problem_info to include checker language
-- Since problem_info is JSON, we'll handle this in code