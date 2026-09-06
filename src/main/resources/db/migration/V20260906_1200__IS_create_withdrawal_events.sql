CREATE TABLE withdrawal_events (
    id             BIGSERIAL   PRIMARY KEY,
    user_id        BIGINT,
    withdrawn_at   TIMESTAMPTZ NOT NULL,
    anonymized_at  TIMESTAMPTZ,
    expires_at     TIMESTAMPTZ NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_withdrawal_events_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT uq_withdrawal_events_user_withdrawn_at
        UNIQUE (user_id, withdrawn_at),
    CONSTRAINT chk_withdrawal_events_expiration
        CHECK (expires_at > withdrawn_at),
    CONSTRAINT chk_withdrawal_events_anonymization
        CHECK (
            anonymized_at IS NULL
            OR (user_id IS NULL AND anonymized_at >= withdrawn_at)
        )
);

CREATE TABLE withdrawal_event_reasons (
    id             BIGSERIAL   PRIMARY KEY,
    event_id       BIGINT      NOT NULL,
    reason         VARCHAR(50) NOT NULL,
    custom_reason  VARCHAR(200),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_withdrawal_event_reasons_event
        FOREIGN KEY (event_id) REFERENCES withdrawal_events(id) ON DELETE CASCADE,
    CONSTRAINT uq_withdrawal_event_reasons_event_reason
        UNIQUE (event_id, reason),
    CONSTRAINT chk_withdrawal_event_reasons_other_custom_reason
        CHECK (
            (reason = 'OTHER' AND custom_reason IS NOT NULL AND LENGTH(BTRIM(custom_reason)) > 0)
            OR
            (reason <> 'OTHER' AND custom_reason IS NULL)
        )
);

CREATE INDEX idx_withdrawal_events_withdrawn_at
    ON withdrawal_events (withdrawn_at DESC);

CREATE INDEX idx_withdrawal_events_expires_at
    ON withdrawal_events (expires_at);

CREATE INDEX idx_withdrawal_event_reasons_reason_event_id
    ON withdrawal_event_reasons (reason, event_id);

COMMENT ON TABLE withdrawal_events IS
    'Withdrawal request events retained independently from user accounts for up to one year';
COMMENT ON COLUMN withdrawal_events.user_id IS
    'Temporary user link removed when the event is anonymized or the user is hard-deleted';
COMMENT ON COLUMN withdrawal_events.expires_at IS
    'Retention deadline calculated from the withdrawal request time';

-- LEGACY BACKFILL START
INSERT INTO withdrawal_events (
    user_id,
    withdrawn_at,
    expires_at,
    created_at
)
SELECT
    uwr.user_id,
    uwr.withdrawn_at,
    uwr.withdrawn_at + INTERVAL '1 year',
    MIN(uwr.created_at)
FROM user_withdrawal_reasons uwr
GROUP BY uwr.user_id, uwr.withdrawn_at;

INSERT INTO withdrawal_event_reasons (
    event_id,
    reason,
    custom_reason,
    created_at
)
SELECT DISTINCT ON (we.id, uwr.reason)
    we.id,
    uwr.reason,
    uwr.custom_reason,
    uwr.created_at
FROM user_withdrawal_reasons uwr
JOIN withdrawal_events we
  ON we.user_id = uwr.user_id
 AND we.withdrawn_at = uwr.withdrawn_at
ORDER BY we.id, uwr.reason, uwr.created_at DESC, uwr.id DESC;
-- LEGACY BACKFILL END
