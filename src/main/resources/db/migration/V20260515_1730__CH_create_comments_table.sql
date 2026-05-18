CREATE TABLE comments (
    id                BIGSERIAL    PRIMARY KEY,
    daily_report_id   BIGINT       NOT NULL,
    author_id         BIGINT       NOT NULL,
    parent_comment_id BIGINT,
    content           VARCHAR(500) NOT NULL,
    is_secret         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMPTZ,

    CONSTRAINT fk_comments_daily_report    FOREIGN KEY (daily_report_id)   REFERENCES daily_reports(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_author          FOREIGN KEY (author_id)          REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_parent_comment  FOREIGN KEY (parent_comment_id)  REFERENCES comments(id) ON DELETE CASCADE
);

CREATE INDEX idx_comments_daily_report_id   ON comments (daily_report_id);
CREATE INDEX idx_comments_parent_comment_id ON comments (parent_comment_id);