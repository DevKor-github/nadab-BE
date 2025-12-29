-- 1. 이미 soft delete 된 데이터 영구 삭제
DELETE FROM answer_entries WHERE deleted_at IS NOT NULL;

-- 2. 컬럼 삭제
ALTER TABLE answer_entries DROP COLUMN deleted_at;