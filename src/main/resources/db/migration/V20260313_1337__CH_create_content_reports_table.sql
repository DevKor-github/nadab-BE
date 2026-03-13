CREATE TABLE content_reports (
    id BIGSERIAL PRIMARY KEY,

    reporter_id BIGINT NOT NULL,
    reported_user_id BIGINT NOT NULL,
    daily_report_id BIGINT NOT NULL,

    reason VARCHAR(50) NOT NULL,
    custom_reason VARCHAR(200),

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_content_reports_reporter_daily_report UNIQUE (reporter_id, daily_report_id),
    CONSTRAINT fk_content_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_content_reports_reported_user FOREIGN KEY (reported_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_content_reports_daily_report FOREIGN KEY (daily_report_id) REFERENCES daily_reports(id) ON DELETE CASCADE
);

CREATE INDEX idx_content_reports_reported_user ON content_reports(reported_user_id);