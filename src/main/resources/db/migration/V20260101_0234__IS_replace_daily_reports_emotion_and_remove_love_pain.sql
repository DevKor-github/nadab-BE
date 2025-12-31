-- 1) daily_reports.emotion_id 를 '기타(ETC)'로 치환
UPDATE daily_reports
SET emotion_id = (
    SELECT id
    FROM emotions
    WHERE code = 'ETC'
)
WHERE emotion_id IN (
    SELECT id
    FROM emotions
    WHERE code IN ('LOVE', 'PAIN')
);

-- 2) emotions에서 '사랑(LOVE)', '고통(PAIN)' 레코드 삭제
DELETE FROM emotions
WHERE code IN ('LOVE', 'PAIN');