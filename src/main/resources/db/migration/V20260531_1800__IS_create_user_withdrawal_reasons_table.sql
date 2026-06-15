CREATE TABLE user_withdrawal_reasons (
    id            BIGSERIAL    PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    reason        VARCHAR(50)  NOT NULL,
    custom_reason VARCHAR(200),
    withdrawn_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user_withdrawal_reasons_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT chk_user_withdrawal_reasons_other_custom_reason
        CHECK (
            (reason = 'OTHER' AND custom_reason IS NOT NULL AND LENGTH(BTRIM(custom_reason)) > 0)
            OR
            (reason <> 'OTHER' AND custom_reason IS NULL)
        )
);

CREATE INDEX idx_user_withdrawal_reasons_user_id ON user_withdrawal_reasons (user_id);
CREATE INDEX idx_user_withdrawal_reasons_reason_created_at ON user_withdrawal_reasons (reason, created_at DESC);
CREATE INDEX idx_user_withdrawal_reasons_withdrawn_at ON user_withdrawal_reasons (withdrawn_at DESC);
CREATE INDEX idx_user_withdrawal_reasons_user_withdrawn_at ON user_withdrawal_reasons (user_id, withdrawn_at DESC);
