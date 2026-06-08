ALTER TABLE user_withdrawal_reasons
    ADD COLUMN IF NOT EXISTS withdrawn_at TIMESTAMPTZ;

UPDATE user_withdrawal_reasons
SET withdrawn_at = created_at
WHERE withdrawn_at IS NULL;

ALTER TABLE user_withdrawal_reasons
    ALTER COLUMN withdrawn_at SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_user_withdrawal_reasons_withdrawn_at
    ON user_withdrawal_reasons (withdrawn_at DESC);

CREATE INDEX IF NOT EXISTS idx_user_withdrawal_reasons_user_withdrawn_at
    ON user_withdrawal_reasons (user_id, withdrawn_at DESC);
