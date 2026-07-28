WITH target_users AS (
    SELECT u.id AS user_id
    FROM users u
    WHERE NOT EXISTS (
        SELECT 1
        FROM ask_chat_wallet_logs l
        WHERE l.user_id = u.id
          AND l.reason = 'INITIAL_FREE_GRANT'
    )
),
inserted_wallets AS (
    INSERT INTO ask_chat_wallets (
        user_id,
        free_turn_balance,
        paid_turn_balance,
        version,
        created_at,
        updated_at
    )
    SELECT
        user_id,
        0,
        0,
        0,
        NOW(),
        NOW()
    FROM target_users
    ON CONFLICT (user_id) DO NOTHING
    RETURNING user_id
),
updated_wallets AS (
    UPDATE ask_chat_wallets w
    SET free_turn_balance = w.free_turn_balance + 3,
        updated_at = NOW(),
        version = w.version + 1
    FROM target_users t
    WHERE w.user_id = t.user_id
    RETURNING
        w.user_id,
        w.free_turn_balance,
        w.paid_turn_balance
)
INSERT INTO ask_chat_wallet_logs (
    user_id,
    session_id,
    message_id,
    free_turn_delta,
    paid_turn_delta,
    free_turn_balance_after,
    paid_turn_balance_after,
    reason,
    status,
    ref_type,
    ref_id,
    idempotency_key,
    created_at
)
SELECT
    user_id,
    NULL,
    NULL,
    3,
    0,
    free_turn_balance,
    paid_turn_balance,
    'INITIAL_FREE_GRANT',
    'CONFIRMED',
    'SIGNUP',
    user_id,
    'ask-chat-initial-free-' || user_id,
    NOW()
FROM updated_wallets;
