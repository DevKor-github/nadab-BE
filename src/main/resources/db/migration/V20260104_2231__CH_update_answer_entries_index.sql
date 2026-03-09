-- 검색 성능 최적화 인덱스 변경

-- 기존 인덱스 제거
DROP INDEX IF EXISTS idx_answer_entries_user_date_id;

-- 최적화된 인덱스 생성 (id 제거)
CREATE INDEX idx_answer_entries_user_date
    ON answer_entries(user_id, date DESC);