CREATE TABLE IF NOT EXISTS personal_access_tokens (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id),
    name         VARCHAR(255) NOT NULL,
    token_hash   VARCHAR(64) NOT NULL UNIQUE,
    scopes       VARCHAR(255) NOT NULL DEFAULT 'read:write',
    revoked      BOOLEAN NOT NULL DEFAULT FALSE,
    last_used_at TIMESTAMP,
    created_at   TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_pat_user_id ON personal_access_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_pat_token_hash ON personal_access_tokens(token_hash);
