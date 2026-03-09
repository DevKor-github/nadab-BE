-- (email, verification_type) 조합의 중복 방지

-- 기존 일반 INDEX 삭제
DROP INDEX IF EXISTS idx_email_verifications_email_type;

-- UNIQUE INDEX 생성
CREATE UNIQUE INDEX idx_email_verifications_email_type_unique
ON email_verifications(email, verification_type);