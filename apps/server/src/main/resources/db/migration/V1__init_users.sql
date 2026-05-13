CREATE TABLE users (
    id         BIGSERIAL    PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    name       VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
