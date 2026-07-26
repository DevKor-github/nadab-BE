CREATE TABLE ask_chat_wallets (
    id                 BIGSERIAL   PRIMARY KEY,
    user_id            BIGINT      NOT NULL UNIQUE,
    free_turn_balance  INTEGER     NOT NULL DEFAULT 0,
    paid_turn_balance  INTEGER     NOT NULL DEFAULT 0,
    version            BIGINT      NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_ask_chat_wallets_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_ask_chat_wallets_free_turn_balance_non_negative
        CHECK (free_turn_balance >= 0),
    CONSTRAINT chk_ask_chat_wallets_paid_turn_balance_non_negative
        CHECK (paid_turn_balance >= 0)
);

CREATE TABLE ask_chat_wallet_logs (
    id                       BIGSERIAL    PRIMARY KEY,
    user_id                  BIGINT       NOT NULL,
    session_id               BIGINT,
    message_id               BIGINT,
    free_turn_delta          INTEGER      NOT NULL,
    paid_turn_delta          INTEGER      NOT NULL,
    free_turn_balance_after  INTEGER      NOT NULL,
    paid_turn_balance_after  INTEGER      NOT NULL,
    reason                   VARCHAR(64)  NOT NULL,
    status                   VARCHAR(16)  NOT NULL,
    ref_type                 VARCHAR(64),
    ref_id                   BIGINT,
    idempotency_key          VARCHAR(128),
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_ask_chat_wallet_logs_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ask_chat_wallet_logs_session
        FOREIGN KEY (session_id) REFERENCES ask_chat_sessions(id) ON DELETE SET NULL,
    CONSTRAINT fk_ask_chat_wallet_logs_message
        FOREIGN KEY (message_id) REFERENCES ask_chat_messages(id) ON DELETE SET NULL,
    CONSTRAINT chk_ask_chat_wallet_logs_free_turn_balance_after_non_negative
        CHECK (free_turn_balance_after >= 0),
    CONSTRAINT chk_ask_chat_wallet_logs_paid_turn_balance_after_non_negative
        CHECK (paid_turn_balance_after >= 0)
);

CREATE INDEX idx_ask_chat_wallet_logs_user_created
    ON ask_chat_wallet_logs(user_id, created_at DESC);

CREATE INDEX idx_ask_chat_wallet_logs_session_created
    ON ask_chat_wallet_logs(session_id, created_at DESC);

CREATE UNIQUE INDEX uk_ask_chat_wallet_logs_idempotency_key
    ON ask_chat_wallet_logs(idempotency_key)
    WHERE idempotency_key IS NOT NULL;
