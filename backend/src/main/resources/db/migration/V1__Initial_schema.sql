-- Initial schema migration

CREATE TABLE users (
    user_id BIGSERIAL NOT NULL,
    handle VARCHAR(50) NOT NULL UNIQUE,
    hashed_password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id)
);

CREATE TABLE contests (
    contest_id BIGSERIAL NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    PRIMARY KEY (contest_id)
);

CREATE TABLE problems (
    problem_id BIGSERIAL NOT NULL,
    title VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    problem_info JSON NOT NULL,
    changelog JSON NOT NULL,
    invocations JSON,
    PRIMARY KEY (problem_id)
);

CREATE TABLE files (
    file_id BIGSERIAL NOT NULL,
    format VARCHAR(255) NOT NULL,
    content BYTEA NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    PRIMARY KEY (file_id)
);

CREATE TABLE contest_problems (
    id BIGSERIAL NOT NULL,
    contest_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    order_index INT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (contest_id) REFERENCES contests (contest_id),
    FOREIGN KEY (problem_id) REFERENCES problems (problem_id)
);

CREATE TABLE contest_users (
    id BIGSERIAL NOT NULL,
    contest_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (contest_id, user_id),
    FOREIGN KEY (contest_id) REFERENCES contests (contest_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE problem_users (
    id BIGSERIAL NOT NULL,
    problem_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    role VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (problem_id, user_id),
    FOREIGN KEY (problem_id) REFERENCES problems (problem_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);