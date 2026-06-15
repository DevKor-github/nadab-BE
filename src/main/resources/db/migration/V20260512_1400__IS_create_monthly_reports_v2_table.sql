CREATE TABLE monthly_reports_v2 (
                                    id BIGSERIAL PRIMARY KEY,

                                    user_id BIGINT NOT NULL,
                                    month_start_date DATE NOT NULL,
                                    month_end_date DATE NOT NULL,

    -- 리포트 생성 기준일
                                    date DATE NOT NULL,

    -- 1. 이미지
                                    image_key VARCHAR(255),
                                    image_status VARCHAR(16) NOT NULL DEFAULT 'PENDING',

    -- 2~4. v2 리포트
                                    content JSONB NOT NULL,
                                    emotion_summary_content JSONB NOT NULL,
                                    emotion_stats JSONB NOT NULL,


    -- 조회/목록 최적화용 캐시 컬럼
                                    summary VARCHAR(80) NOT NULL,
                                    comment_summary VARCHAR(80) NOT NULL,
                                    dominant_keyword VARCHAR(30) NOT NULL,

    -- 이전 월간 리포트 비교 상태
                                    comparison_type VARCHAR(20) NOT NULL DEFAULT 'BASELINE',

    -- 생성 상태
                                    status VARCHAR(16) NOT NULL,

                                    analyzed_at TIMESTAMPTZ,
                                    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                                    CONSTRAINT uq_monthly_reports_v2_user_month
                                        UNIQUE (user_id, month_start_date),

                                    CONSTRAINT fk_monthly_reports_v2_user
                                        FOREIGN KEY (user_id) REFERENCES users(id)
);