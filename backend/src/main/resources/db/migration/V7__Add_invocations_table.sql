-- Create invocations table to store invocation history
CREATE TABLE invocations (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    problem_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITHOUT TIME ZONE,
    solution_ids JSON NOT NULL,
    test_ids JSON NOT NULL,
    results JSON,
    error_message TEXT,
    FOREIGN KEY (problem_id) REFERENCES problems (problem_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);

-- Create index for faster queries
CREATE INDEX idx_invocations_problem_id ON invocations (problem_id);
CREATE INDEX idx_invocations_user_id ON invocations (user_id);
CREATE INDEX idx_invocations_status ON invocations (status);