-- user_wallets: 유저당 1행(현재 잔액의 단일 진실)
CREATE TABLE user_wallets (
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT NOT NULL UNIQUE,
                              crystal_balance BIGINT NOT NULL DEFAULT 0,
                              version BIGINT NOT NULL DEFAULT 0,
                              created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                              updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE user_wallets
    ADD CONSTRAINT fk_user_wallets_user
        FOREIGN KEY (user_id) REFERENCES users(id)
            ON DELETE CASCADE;

-- crystal_logs: 변동 발생 시만 기록(운영/디버깅용)
CREATE TABLE crystal_logs (
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              delta BIGINT NOT NULL,
                              balance_after BIGINT NOT NULL,
                              reason VARCHAR(64) NOT NULL,
                              ref_type VARCHAR(64),
                              ref_id BIGINT,
                              created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_crystal_logs_user_id_created_at
    ON crystal_logs(user_id, created_at DESC);

ALTER TABLE crystal_logs
    ADD CONSTRAINT fk_crystal_logs_user
        FOREIGN KEY (user_id) REFERENCES users(id)
            ON DELETE CASCADE;