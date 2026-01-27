-- 피드 공유 기능을 위한 is_shared 컬럼 추가
ALTER TABLE daily_reports
ADD COLUMN is_shared BOOLEAN NOT NULL DEFAULT FALSE;

-- 피드 조회 성능 최적화를 위한 인덱스
CREATE INDEX idx_daily_reports_date_shared_sharing
ON daily_reports(date, is_shared);
