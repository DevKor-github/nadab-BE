-- 검색 이력 테이블 생성
CREATE TABLE search_histories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    keyword VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_search_histories_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT uq_search_histories_user_keyword
        UNIQUE (user_id, keyword)
);

-- 최근 검색어 조회 최적화 (user별 최신순 10개)
CREATE INDEX idx_search_histories_user_updated
    ON search_histories(user_id, updated_at DESC);