CREATE TABLE social_suspensions (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    started_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ  NOT NULL,

    CONSTRAINT fk_social_suspensions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_social_suspensions_user_expires ON social_suspensions(user_id, expires_at);
CREATE INDEX idx_social_suspensions_expires ON social_suspensions(expires_at);