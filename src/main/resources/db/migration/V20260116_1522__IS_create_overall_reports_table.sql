CREATE TABLE overall_reports (
                                 id BIGSERIAL PRIMARY KEY,
                                 user_id BIGINT NOT NULL,
                                 analysis_type_id BIGINT,

                                 snapshot_date DATE NOT NULL, -- 이 날짜까지의 기록을 기반으로 생성

                                 type_analysis VARCHAR(400),
                                 persona1_title VARCHAR(15),
                                 persona1_content VARCHAR(300),
                                 persona2_title VARCHAR(15),
                                 persona2_content VARCHAR(300),

                                 status VARCHAR(16) NOT NULL,
                                 analyzed_at TIMESTAMPTZ,
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 한 유저가 같은 snapshot_date로 중복 생성 방지
CREATE UNIQUE INDEX uq_overall_reports_user_snapshot
    ON overall_reports(user_id, snapshot_date);

CREATE INDEX idx_overall_reports_user_created
    ON overall_reports(user_id, created_at DESC);

ALTER TABLE overall_reports
    ADD CONSTRAINT fk_overall_reports_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE overall_reports
    ADD CONSTRAINT fk_overall_reports_analysis_type
        FOREIGN KEY (analysis_type_id) REFERENCES analysis_types(id);
