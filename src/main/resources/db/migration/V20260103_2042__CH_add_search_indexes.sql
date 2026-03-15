-- 검색 성능 최적화 인덱스 추가

-- 기존 단일 인덱스 제거 (복합 인덱스로 대체)
DROP INDEX IF EXISTS idx_answer_entries_user_id;

-- 답변 검색 최적화: user별 최신순 조회 및 Cursor-based Pagination
CREATE INDEX idx_answer_entries_user_date_id
    ON answer_entries(user_id, date DESC, id DESC);