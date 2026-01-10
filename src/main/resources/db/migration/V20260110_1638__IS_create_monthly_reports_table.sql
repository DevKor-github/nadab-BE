CREATE TABLE monthly_reports (
                                 id BIGSERIAL PRIMARY KEY,
                                 user_id BIGINT NOT NULL,

                                 month_start_date DATE NOT NULL,
                                 month_end_date   DATE NOT NULL,

                                 discovered VARCHAR(250),
                                 good       VARCHAR(250),
                                 improve    VARCHAR(250),

                                 status VARCHAR(16) NOT NULL,
                                 date   DATE NOT NULL,
                                 analyzed_at TIMESTAMPTZ,

                                 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                 CONSTRAINT fk_monthly_reports_user
                                     FOREIGN KEY (user_id) REFERENCES users(id),

                                 CONSTRAINT uq_monthly_reports_user_month
                                     UNIQUE (user_id, month_start_date)
);

CREATE INDEX idx_monthly_reports_user_month_start
    ON monthly_reports(user_id, month_start_date DESC);