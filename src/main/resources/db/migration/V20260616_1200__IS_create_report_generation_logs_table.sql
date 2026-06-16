CREATE TABLE report_generation_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    report_type VARCHAR(32) NOT NULL,
    report_id BIGINT,
    step VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    llm_provider VARCHAR(32),
    llm_model VARCHAR(128),
    error_code VARCHAR(128),
    exception_class VARCHAR(255),
    http_status INTEGER,
    external_error_code VARCHAR(128),
    elapsed_ms BIGINT,
    metadata JSONB,
    started_at TIMESTAMPTZ NOT NULL,
    ended_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE report_generation_logs
    ADD CONSTRAINT fk_report_generation_logs_user
        FOREIGN KEY (user_id) REFERENCES users(id)
            ON DELETE SET NULL;

CREATE INDEX idx_report_generation_logs_report
    ON report_generation_logs(report_type, report_id, created_at DESC);

CREATE INDEX idx_report_generation_logs_user_created_at
    ON report_generation_logs(user_id, created_at DESC);

CREATE INDEX idx_report_generation_logs_status_created_at
    ON report_generation_logs(status, created_at DESC);

CREATE INDEX idx_report_generation_logs_error_code_created_at
    ON report_generation_logs(error_code, created_at DESC);
