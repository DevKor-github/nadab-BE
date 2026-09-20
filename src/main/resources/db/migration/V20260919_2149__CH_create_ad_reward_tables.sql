CREATE TABLE ad_reward_sessions (
    id             BIGSERIAL PRIMARY KEY,

    user_id        BIGINT       NOT NULL,

    session_key    VARCHAR(80)  NOT NULL,
    feature        VARCHAR(40)  NOT NULL,
    reward_amount  BIGINT       NOT NULL,

    status         VARCHAR(20)  NOT NULL,
    transaction_id VARCHAR(120),

    expires_at     TIMESTAMPTZ  NOT NULL,
    rewarded_at    TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_ad_reward_sessions_session_key UNIQUE (session_key),
    CONSTRAINT uq_ad_reward_sessions_transaction_id UNIQUE (transaction_id),

    CONSTRAINT fk_ad_reward_sessions_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- createSession마다 도는 expirePendingSessions(WHERE user_id) + 유저 삭제 CASCADE 조회용
CREATE INDEX idx_ad_reward_sessions_user_id ON ad_reward_sessions (user_id);


CREATE TABLE ad_reward_logs (
    id             BIGSERIAL PRIMARY KEY,

    user_id        BIGINT       NOT NULL,
    session_id     BIGINT       NOT NULL,

    event          VARCHAR(30)  NOT NULL,
    detail         VARCHAR(255),
    transaction_id VARCHAR(120),

    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT fk_ad_reward_logs_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT fk_ad_reward_logs_session
        FOREIGN KEY (session_id) REFERENCES ad_reward_sessions(id) ON DELETE CASCADE
);