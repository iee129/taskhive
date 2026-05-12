CREATE TABLE task_activities (
    id          BIGSERIAL    PRIMARY KEY,
    task_id     BIGINT       NOT NULL,
    task_title  VARCHAR(255),
    actor_email VARCHAR(255) NOT NULL,
    action      VARCHAR(50)  NOT NULL,
    detail      TEXT,
    occurred_at TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_tasks_status     ON tasks(status);
CREATE INDEX idx_tasks_priority   ON tasks(priority);
CREATE INDEX idx_tasks_deleted_at ON tasks(deleted_at);
CREATE INDEX idx_tasks_assignee   ON tasks(assignee_id);
