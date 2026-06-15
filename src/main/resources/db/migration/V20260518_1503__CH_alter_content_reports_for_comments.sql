-- comment_id 컬럼 추가
ALTER TABLE content_reports ADD COLUMN comment_id BIGINT;

-- daily_report_id NOT NULL → nullable
ALTER TABLE content_reports ALTER COLUMN daily_report_id DROP NOT NULL;

-- 기존 UNIQUE 제약 제거
ALTER TABLE content_reports DROP CONSTRAINT uq_content_reports_reporter_daily_report;

-- daily_report_id FK: ON DELETE CASCADE → ON DELETE SET NULL
ALTER TABLE content_reports DROP CONSTRAINT fk_content_reports_daily_report;
ALTER TABLE content_reports
    ADD CONSTRAINT fk_content_reports_daily_report
        FOREIGN KEY (daily_report_id) REFERENCES daily_reports(id) ON DELETE SET NULL;

-- comment_id FK
ALTER TABLE content_reports
    ADD CONSTRAINT fk_content_reports_comment
        FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE SET NULL;

-- 게시글 신고 partial unique (게시글 신고에서 중복 방지)
CREATE UNIQUE INDEX uq_content_reports_reporter_daily_report
    ON content_reports(reporter_id, daily_report_id)
    WHERE daily_report_id IS NOT NULL AND comment_id IS NULL;

-- 댓글 신고 partial unique (댓글 신고에서 중복 방지)
CREATE UNIQUE INDEX uq_content_reports_reporter_comment
    ON content_reports(reporter_id, comment_id)
    WHERE comment_id IS NOT NULL;