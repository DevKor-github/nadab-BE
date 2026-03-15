-- 1) 기존 UNIQUE 제약 삭제
ALTER TABLE interests
DROP CONSTRAINT IF EXISTS interests_code_key;

-- 2) 새로운 UNIQUE 제약 추가 (명시적 이름)
ALTER TABLE interests
    ADD CONSTRAINT uk_interests_code UNIQUE (code);
