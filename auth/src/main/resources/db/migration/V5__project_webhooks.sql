CREATE TABLE IF NOT EXISTS project_webhooks (
    id                   BIGSERIAL PRIMARY KEY,
    project_id           BIGINT NOT NULL REFERENCES projects(id),
    url                  VARCHAR(500) NOT NULL,
    secret               VARCHAR(128),
    events               VARCHAR(255) NOT NULL DEFAULT 'task.created,task.updated,task.deleted',
    enabled              BOOLEAN NOT NULL DEFAULT TRUE,
    consecutive_failures INTEGER NOT NULL DEFAULT 0,
    created_at           TIMESTAMP NOT NULL,
    updated_at           TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_webhook_project_id ON project_webhooks(project_id);
