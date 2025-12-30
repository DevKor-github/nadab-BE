-- 1. date 컬럼 추가 (먼저 Nullable로 생성)
ALTER TABLE answer_entries ADD COLUMN date DATE;

-- 2. 기존 created_at 값을 기준으로 date 데이터 채우기 (Backfill)
UPDATE answer_entries
SET date = created_at::DATE;

-- 3. 데이터가 채워졌으므로 NOT NULL 제약 조건 적용
ALTER TABLE answer_entries
    ALTER COLUMN date SET NOT NULL;

-- 4. (user_id, date) 유니크 제약 조건 추가
-- 한 유저는 하루에 하나의 답변만 작성할 수 있다는 의미
ALTER TABLE answer_entries
    ADD CONSTRAINT uq_answer_entries_user_id_date UNIQUE (user_id, date);