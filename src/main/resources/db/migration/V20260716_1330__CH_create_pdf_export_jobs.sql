CREATE TABLE pdf_export_jobs (
    id             BIGSERIAL PRIMARY KEY,

    user_id        BIGINT       NOT NULL,

    type           VARCHAR(32)  NOT NULL,
    start_date     DATE         NOT NULL,
    end_date       DATE         NOT NULL,

    status         VARCHAR(16)  NOT NULL,

    result_key     VARCHAR(255) NOT NULL,
    crystal_log_id BIGINT,
    error_code     VARCHAR(128),

    completed_at   TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT fk_pdf_export_jobs_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uq_pdf_export_jobs_idem
    ON pdf_export_jobs (user_id, type, start_date, end_date)
    WHERE status IN ('PENDING', 'IN_PROGRESS');

CREATE INDEX idx_pdf_export_jobs_user_created_at
    ON pdf_export_jobs (user_id, created_at DESC);

CREATE INDEX idx_pdf_export_jobs_status_updated_at
    ON pdf_export_jobs (status, updated_at);