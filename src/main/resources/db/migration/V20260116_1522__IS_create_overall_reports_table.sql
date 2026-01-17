CREATE TABLE overall_reports (
                                 id BIGSERIAL PRIMARY KEY,
                                 user_id BIGINT NOT NULL,
                                 analysis_type_id BIGINT,
                                 interest_code VARCHAR(50) NOT NULL,

                                 date DATE NOT NULL,

                                 type_analysis VARCHAR(400),
                                 persona1_title VARCHAR(15),
                                 persona1_content VARCHAR(300),
                                 persona2_title VARCHAR(15),
                                 persona2_content VARCHAR(300),

                                 status VARCHAR(16) NOT NULL,
                                 analyzed_at TIMESTAMPTZ,

                                 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                 deleted_at TIMESTAMPTZ,

                                 CONSTRAINT fk_overall_reports_user
                                     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

                                 CONSTRAINT fk_overall_reports_analysis_type
                                     FOREIGN KEY (analysis_type_id) REFERENCES analysis_types(id)
);

-- 삭제되지 않은 현재 리포트는 interest_code당 1개 (부분 유니크 인덱스)
CREATE UNIQUE INDEX uq_overall_reports_user_interest_active
    ON overall_reports (user_id, interest_code)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_overall_reports_not_deleted
    ON overall_reports (deleted_at);

CREATE INDEX idx_overall_reports_user_interest_date
    ON overall_reports (user_id, interest_code, date DESC);