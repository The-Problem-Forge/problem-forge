CREATE TABLE "users"(
    "user_id" BIGINT NOT NULL,
    "email" VARCHAR(100) NOT NULL,
    "handle" VARCHAR(40) NOT NULL,
    "password" VARCHAR(255) NOT NULL
);
ALTER TABLE
    "users" ADD PRIMARY KEY("user_id");


CREATE TABLE contests (
    "contest_id" BIGINT NOT NULL,
    "title" VARCHAR(255) NOT NULL,
    "owner" BIGINT NOT NULL REFERENCES users (user_id),
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "modified_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "contests" ADD PRIMARY KEY("contest_id");


CREATE TABLE "problems_master"(
    "problem_id" BIGINT NOT NULL,
    "tag" VARCHAR(40) NOT NULL,
    "owner" BIGINT NOT NULL REFERENCES users (user_id)
    "problem_info" JSON NOT NULL,
    "changelog" JSON NOT NULL
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "modified_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "problems_master" ADD PRIMARY KEY("problem_id");

CREATE TABLE "files" (
    "file_id" BIGINT NOT NULL,
    "checksum" VARCHAR(255) NOT NULL,
    "format" VARCHAR(255) NOT NULL,
    "type" VARCHAR(255) NOT NULL,
    "content" BYTEA NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "modified_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "files" ADD PRIMARY KEY("file_id");

CREATE TABLE "problems_branch"(
    "branch_id" BIGINT NOT NULL,
    "problem_id" BIGINT NOT NULL REFERENCES problems_master (problem_id), 
    "owner" BIGINT NOT NULL REFERENCES users (user_id),
    "problem_info" JSON NOT NULL,
    "current_changes" JSON NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "modified_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "problem_branch" ADD PRIMARY KEY("branch_id");


CREATE TABLE "contest_problems"(
    "contest_id" BIGINT NOT NULL REFERENCES contests (contest_id),
    "problem_id" BIGINT NOT NULL REFERENCES problems_master (problem_id),
    "order_index" INT NOT NULL DEFAULT 0,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "contest_problems" ADD PRIMARY KEY("contest_id", "problem_id");


CREATE TABLE "problem_users"(
    "problem_id" BIGINT NOT NULL,
    "user_id" BIGINT NOT NULL,
    "role" CHAR(255) NOT NULL
    "modified_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "problem_users" ADD PRIMARY KEY("problem_id");


CREATE TABLE "contest_users"(
    "contest_id" BIGINT NOT NULL REFERENCES constests (contest_id),
    "user_id" BIGINT NOT NULL,
    "role" VARCHAR(255) NOT NULL
);
ALTER TABLE
    "contest_users" ADD PRIMARY KEY("contest_id");

-- Solutions and attachments do not have problem_branch id, they only have links from json?
-- Because one solution can exist in multiple branches.
-- Problem, when to delete solution from branch? Simple answer - never.

-- Problem, who can modify solution? If user has problem branch and made change - he got new solution.
-- But if other users will know id then they can modify it. 
-- Firstly we can store problem_id to check permissions. By problem_id and user_id we can check if user has permission to problem,
-- And check 

CREATE TABLE "solutions" (
    "solution_id" BIGINT NOT NULL,
    "problem_id" BIGINT NOT NULL REFERENCES problem_master (problem_id),
    "file_id" BIGINT NOT NULL REFERENCES files (file_id),
    "solution_type" VARCHAR(255) NOT NULL,
    "owner" BIGINT NOT NULL REFERENCES users(user_id),
    "is_deleted" BOOLEAN NOT NULL -- If deleted on last iteration. Can be true only on branch, not master
);
ALTER TABLE
    "solutions" ADD PRIMARY KEY("solution_id");

CREATE TABLE "attachments"(
    "attachment_id" BIGINT NOT NULL,
    "problem_id" BIGINT NOT NULL REFERENCES problem_master (problem_id),
    "modified" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "file_id" BIGINT NOT NULL REFERENCES files (file_id),
    "is_deleted" BIGINT NOT NULL
);
ALTER TABLE
    "attachments" ADD PRIMARY KEY("attachment_id");

CREATE TABLE "invocations"(
    "invocation_id" BIGINT NOT NULL,
    "branch_id" BIGINT NOT NULL REFERENCES problem_branch (branch_id),
    "content" JSON NOT NULL,
    "status" VARCHAR(255) NOT NULL
);
ALTER TABLE
    "invocations" ADD PRIMARY KEY("invocation_id");
