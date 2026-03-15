-- 1) users 테이블에서 소셜 관련 컬럼 제거
ALTER TABLE users
DROP COLUMN IF EXISTS provider,
    DROP COLUMN IF EXISTS provider_id;

-- 2) social_account 테이블 생성
CREATE TABLE social_account (
                                id                BIGSERIAL      PRIMARY KEY,
                                user_id           BIGINT         NOT NULL,
                                provider_user_id  VARCHAR(255)   NOT NULL,
                                provider_type     VARCHAR(32)    NOT NULL,
                                refresh_token     TEXT,

                                created_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

                                CONSTRAINT fk_social_account_user
                                    FOREIGN KEY (user_id) REFERENCES users (id)
                                    ON DELETE CASCADE,

                                CONSTRAINT uk_social_account_provider_type_user_id
                                    UNIQUE (provider_type, provider_user_id)
);

CREATE INDEX idx_social_account_user_id
    ON social_account (user_id);