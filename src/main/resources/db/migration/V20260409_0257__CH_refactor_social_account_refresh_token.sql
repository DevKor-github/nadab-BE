-- Refactor social_account refresh_token to use KMS Envelope Encryption
-- 기존 TEXT 컬럼을 삭제하고 4개의 BYTEA 컬럼으로 변경

-- 기존 컬럼 삭제
ALTER TABLE social_account DROP COLUMN IF EXISTS refresh_token;

-- 새 컬럼 추가 (KMS Envelope Encryption용)
ALTER TABLE social_account
    ADD COLUMN refresh_token_ciphertext BYTEA,
    ADD COLUMN refresh_token_key BYTEA,
    ADD COLUMN refresh_token_iv BYTEA,
    ADD COLUMN refresh_token_tag BYTEA;