-- 약관 테이블
CREATE TABLE terms (
    id BIGSERIAL PRIMARY KEY,
    terms_type VARCHAR(50) NOT NULL,
    version VARCHAR(20) NOT NULL,
    required BOOLEAN NOT NULL,
    is_active BOOLEAN NOT NULL,
    CONSTRAINT uk_terms_type_version UNIQUE (terms_type, version)
);

-- 사용자별 약관 동의 기록 테이블
CREATE TABLE user_terms (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    terms_id BIGINT NOT NULL,
    agreed BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user_terms_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_terms_term FOREIGN KEY (terms_id)
        REFERENCES terms(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_terms_user_term UNIQUE (user_id, terms_id)
);

-- 인덱스 생성 (약관 재동의 체크 성능 최적화)
CREATE INDEX idx_terms_active_required ON terms(is_active, required);

-- 초기 약관 데이터 (v1.0)
INSERT INTO terms (terms_type, version, required, is_active) VALUES
('SERVICE', '1.0', true, true),
('PRIVACY', '1.0', true, true),
('AGE_VERIFICATION', '1.0', true, true),
('MARKETING', '1.0', false, true);