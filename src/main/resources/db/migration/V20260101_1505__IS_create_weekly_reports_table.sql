CREATE TABLE weekly_reports (
                                id BIGSERIAL PRIMARY KEY,

                                user_id BIGINT NOT NULL,

                                week_start_date DATE NOT NULL,
                                week_end_date   DATE NOT NULL,

                                discovered VARCHAR(100) NOT NULL,
                                good       VARCHAR(100) NOT NULL,
                                improve    VARCHAR(100) NOT NULL,

                                status          VARCHAR(16) NOT NULL,

                                date            DATE NOT NULL,
                                analyzed_at     TIMESTAMPTZ,

                                created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                                CONSTRAINT fk_weekly_reports_user_id
                                    FOREIGN KEY (user_id) REFERENCES users(id),

                                CONSTRAINT uq_weekly_reports_user_id_week_start_date
                                    UNIQUE (user_id, week_start_date)
);
-- 조회 최적화 (유저별 최신 주간리포트)
CREATE INDEX idx_weekly_reports_user_id_created_at
    ON weekly_reports (user_id, created_at DESC);
