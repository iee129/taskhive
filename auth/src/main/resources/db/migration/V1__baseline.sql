-- TaskHive 기준 스키마 (Spring Boot 3.3 + Hibernate 6 + PostgreSQL)
-- 엔티티 매핑: User, Project, ProjectMember, Task, Comment, TaskActivity, RefreshToken, PasswordResetToken

CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(255) NOT NULL DEFAULT 'USER',
    deleted_at  TIMESTAMP,
    email_verified              BOOLEAN     NOT NULL DEFAULT FALSE,
    verification_token          VARCHAR(64),
    verification_token_expires_at TIMESTAMP,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS projects (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    owner_id    BIGINT NOT NULL REFERENCES users(id),
    deleted_at  TIMESTAMP,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS project_members (
    id          BIGSERIAL PRIMARY KEY,
    project_id  BIGINT NOT NULL REFERENCES projects(id),
    user_id     BIGINT NOT NULL REFERENCES users(id),
    role        VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    CONSTRAINT uq_project_member UNIQUE (project_id, user_id)
);

CREATE TABLE IF NOT EXISTS tasks (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    status      VARCHAR(255) NOT NULL DEFAULT 'TODO',
    priority    VARCHAR(255) NOT NULL DEFAULT 'MEDIUM',
    project_id  BIGINT REFERENCES projects(id),
    assignee_id BIGINT REFERENCES users(id),
    due_date    DATE,
    deleted_at  TIMESTAMP,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS comments (
    id          BIGSERIAL PRIMARY KEY,
    content     VARCHAR(1000) NOT NULL,
    task_id     BIGINT NOT NULL REFERENCES tasks(id),
    author_id   BIGINT NOT NULL REFERENCES users(id),
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS task_activities (
    id          BIGSERIAL PRIMARY KEY,
    task_id     BIGINT NOT NULL,
    task_title  VARCHAR(255),
    actor_email VARCHAR(255) NOT NULL,
    action      VARCHAR(50)  NOT NULL,
    detail      TEXT,
    occurred_at TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(512) NOT NULL UNIQUE,
    user_id     BIGINT NOT NULL REFERENCES users(id),
    expires_at  TIMESTAMP NOT NULL,
    created_at  TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id),
    token       VARCHAR(64) NOT NULL UNIQUE,
    expires_at  TIMESTAMP NOT NULL,
    used_at     TIMESTAMP
);
