CREATE TABLE comments (
    id         BIGSERIAL     PRIMARY KEY,
    content    VARCHAR(1000) NOT NULL,
    task_id    BIGINT        NOT NULL REFERENCES tasks(id),
    author_id  BIGINT        NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);
