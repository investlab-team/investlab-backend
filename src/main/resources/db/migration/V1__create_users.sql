CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email CITEXT NOT NULL,
    password_hash TEXT,
    nickname VARCHAR(30),
    status VARCHAR(30) NOT NULL,
    role VARCHAR(10) NOT NULL,
    failed_login_count INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMPTZ,
    withdrawal_requested_at TIMESTAMPTZ,
    deletion_scheduled_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT users_email_uq UNIQUE (email),
    CONSTRAINT users_nickname_uq UNIQUE (nickname),
    CONSTRAINT users_status_check CHECK (status IN ('PENDING_EMAIL', 'PENDING_NICKNAME', 'ACTIVE', 'WITHDRAWAL_PENDING')),
    CONSTRAINT users_role_check CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT users_active_nickname_check CHECK (status <> 'ACTIVE' OR nickname IS NOT NULL),
    CONSTRAINT users_failed_login_count_check CHECK (failed_login_count >= 0)
);
