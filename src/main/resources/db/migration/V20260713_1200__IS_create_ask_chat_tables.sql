CREATE TABLE ask_chat_sessions (
    id                  BIGSERIAL    PRIMARY KEY,
    user_id             BIGINT       NOT NULL,
    status              VARCHAR(16)  NOT NULL,
    answered_turn_count INTEGER      NOT NULL DEFAULT 0,
    ended_at            TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_ask_chat_sessions_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE ask_chat_messages (
    id              BIGSERIAL    PRIMARY KEY,
    session_id      BIGINT       NOT NULL,
    role            VARCHAR(16)  NOT NULL,
    status          VARCHAR(16)  NOT NULL,
    content         TEXT         NOT NULL,
    llm_provider    VARCHAR(32),
    llm_model       VARCHAR(128),
    input_tokens    BIGINT,
    output_tokens   BIGINT,
    total_tokens    BIGINT,
    thinking_tokens BIGINT,
    error_code      VARCHAR(128),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_ask_chat_messages_session
        FOREIGN KEY (session_id) REFERENCES ask_chat_sessions(id) ON DELETE CASCADE
);

CREATE INDEX idx_ask_chat_sessions_user_status_created
    ON ask_chat_sessions(user_id, status, created_at DESC);

CREATE INDEX idx_ask_chat_messages_session_created
    ON ask_chat_messages(session_id, created_at);
