-- 1. date 컬럼을 먼저 Nullable 상태로 추가
ALTER TABLE daily_reports ADD COLUMN date DATE;

-- 2. 기존 created_at 값을 기준으로 date 데이터 채우기 (Backfill)
UPDATE daily_reports
SET date = created_at::DATE;

-- 3. 데이터가 채워졌으므로 NOT NULL 제약 조건 적용
ALTER TABLE daily_reports
    ALTER COLUMN date SET NOT NULL;

-- 4. (answer_entry_id, date) 유니크 제약 조건 추가
ALTER TABLE daily_reports
    ADD CONSTRAINT uq_daily_reports_answer_entry_id_date UNIQUE (answer_entry_id, date);