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
granted_wallets AS (
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
        3,
        0,
        0,
        NOW(),
        NOW()
    FROM target_users
    ON CONFLICT (user_id) DO UPDATE
        SET free_turn_balance = ask_chat_wallets.free_turn_balance + EXCLUDED.free_turn_balance,
            updated_at = NOW(),
            version = ask_chat_wallets.version + 1
    RETURNING
        user_id,
        free_turn_balance,
        paid_turn_balance
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
FROM granted_wallets;
