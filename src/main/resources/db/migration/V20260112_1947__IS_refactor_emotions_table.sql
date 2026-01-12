-- color_code 컬럼 삭제
ALTER TABLE emotions
DROP COLUMN IF EXISTS color_code;


-- 새로운 emotions 레코드들 생성
-- (기존에 존재하는 '즐거움', '후회', '기타'를 제외한 신규 감정만 추가)
INSERT INTO emotions (code, name) VALUES
                                      ('ACHIEVEMENT', '성취'),
                                      ('INTEREST', '흥미'),
                                      ('PEACE', '평온'),
                                      ('WILL', '의지'),
                                      ('DEPRESSION', '우울');


-- 2. 삭제 예정인 감정들의 emotion_id를 '기타(ETC)'의 id로 변경
-- 삭제 대상: 기쁨(JOY), 슬픔(SADNESS), 분노(ANGER), 좌절(FRUSTRATION), 성장(GROWTH)
UPDATE daily_reports
SET emotion_id = (
    SELECT id
    FROM emotions
    WHERE code = 'ETC'
)
WHERE emotion_id IN (
    SELECT id
    FROM emotions
    WHERE code IN ('JOY', 'SADNESS', 'ANGER', 'FRUSTRATION', 'GROWTH')
);


-- 겹치지 않는(삭제 예정인) emotions 레코드 삭제
DELETE FROM emotions
WHERE code IN ('JOY', 'SADNESS', 'ANGER', 'FRUSTRATION', 'GROWTH');