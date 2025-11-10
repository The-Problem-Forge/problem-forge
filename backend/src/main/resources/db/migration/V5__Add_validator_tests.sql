-- Add validator tests table

CREATE TABLE validator_tests (
    test_id BIGSERIAL NOT NULL,
    problem_id BIGINT NOT NULL,
    input TEXT NOT NULL,
    verdict VARCHAR(50) NOT NULL DEFAULT 'VALID',
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    PRIMARY KEY (test_id),
    FOREIGN KEY (problem_id) REFERENCES problems (problem_id)
);