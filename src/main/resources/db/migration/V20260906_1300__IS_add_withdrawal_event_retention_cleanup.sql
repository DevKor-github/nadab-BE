ALTER TABLE withdrawal_event_reasons
    DROP CONSTRAINT chk_withdrawal_event_reasons_other_custom_reason;

ALTER TABLE withdrawal_event_reasons
    ADD CONSTRAINT chk_withdrawal_event_reasons_other_custom_reason
        CHECK (
            (
                reason = 'OTHER'
                AND (
                    custom_reason IS NULL
                    OR LENGTH(BTRIM(custom_reason)) > 0
                )
            )
            OR
            (reason <> 'OTHER' AND custom_reason IS NULL)
        );

COMMENT ON COLUMN withdrawal_event_reasons.custom_reason IS
    'Optional free-text detail removed after the 14-day account recovery period';

-- RETENTION CLEANUP START
DELETE FROM withdrawal_events
WHERE expires_at <= CURRENT_TIMESTAMP;

DELETE FROM user_withdrawal_reasons
WHERE withdrawn_at < CURRENT_TIMESTAMP - INTERVAL '14 days';

UPDATE withdrawal_event_reasons wer
SET custom_reason = NULL
FROM withdrawal_events we
WHERE we.id = wer.event_id
  AND we.withdrawn_at < CURRENT_TIMESTAMP - INTERVAL '14 days'
  AND wer.custom_reason IS NOT NULL;

UPDATE withdrawal_events
SET user_id = NULL,
    anonymized_at = COALESCE(anonymized_at, CURRENT_TIMESTAMP)
WHERE withdrawn_at < CURRENT_TIMESTAMP - INTERVAL '14 days'
  AND (user_id IS NOT NULL OR anonymized_at IS NULL);
-- RETENTION CLEANUP END
