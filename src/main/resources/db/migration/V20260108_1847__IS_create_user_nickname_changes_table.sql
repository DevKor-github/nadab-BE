-- 닉네임 변경 이력 테이블
CREATE TABLE user_nickname_changes (
                                       id BIGSERIAL PRIMARY KEY,
                                       user_id BIGINT NOT NULL,
                                       old_nickname VARCHAR(255),
                                       new_nickname VARCHAR(255) NOT NULL,
                                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                       CONSTRAINT fk_user_nickname_changes_user
                                           FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 14일 내 변경 횟수 조회 최적화 (user별 최신순 + 기간필터)
CREATE INDEX idx_user_nickname_changes_user_created_at
    ON user_nickname_changes (user_id, created_at DESC);