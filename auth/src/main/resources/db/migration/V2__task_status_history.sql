CREATE TABLE IF NOT EXISTS task_status_history (
    id          BIGSERIAL PRIMARY KEY,
    task_id     BIGINT NOT NULL REFERENCES tasks(id),
    from_status VARCHAR(50),
    to_status   VARCHAR(50) NOT NULL,
    changed_by  VARCHAR(255) NOT NULL,
    changed_at  TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_task_status_history_task_id_changed_at
    ON task_status_history(task_id, changed_at);
