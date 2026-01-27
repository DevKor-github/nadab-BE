CREATE TABLE friend_search_histories (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,
    searched_user_id BIGINT NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_friend_search_histories_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friend_search_histories_searched_user
        FOREIGN KEY (searched_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_friend_search_histories_user_searched
        UNIQUE (user_id, searched_user_id)
);

-- 최근 검색한 유저 조회 최적화 (user별 최신순)
CREATE INDEX idx_friend_search_histories_user_updated
    ON friend_search_histories(user_id, updated_at DESC);