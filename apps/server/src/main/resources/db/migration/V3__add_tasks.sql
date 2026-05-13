CREATE TABLE tasks (
    id          BIGSERIAL    PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'TODO',
    priority    VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    project_id  BIGINT       REFERENCES projects(id),
    assignee_id BIGINT       REFERENCES users(id),
    due_date    DATE,
    deleted_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
