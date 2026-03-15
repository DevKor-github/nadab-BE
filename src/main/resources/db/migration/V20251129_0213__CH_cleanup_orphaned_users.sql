-- 이전 마이그레이션에서 provider/provider_id 정보가 손실된 사용자들을 정리

-- refresh_tokens 먼저 삭제 (FK 제약 조건)
DELETE FROM refresh_tokens
WHERE user_id IN (
    SELECT u.id
    FROM users u
    LEFT JOIN social_account sa ON u.id = sa.user_id
    WHERE sa.id IS NULL
);

-- social_account에 매핑되어 있지 않은 사용자 모두 삭제
DELETE FROM users
WHERE id NOT IN (SELECT user_id FROM social_account);