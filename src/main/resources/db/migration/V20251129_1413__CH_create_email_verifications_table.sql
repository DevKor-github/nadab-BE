CREATE TABLE email_verifications (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    verification_type VARCHAR(50) NOT NULL,
    verification_code VARCHAR(6) NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 이메일 + 인증 타입으로 조회 (재발송 시 기존 레코드 삭제용)
CREATE INDEX idx_email_verifications_email_type ON email_verifications(email, verification_type);