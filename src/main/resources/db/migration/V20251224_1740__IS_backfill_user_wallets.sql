-- 기존 유저들의 user_wallets 테이블을 백필
INSERT INTO user_wallets (user_id, crystal_balance, version, created_at, updated_at)
SELECT u.id, 0, 0, NOW(), NOW()
FROM users u
         LEFT JOIN user_wallets w ON w.user_id = u.id
WHERE w.user_id IS NULL;
