-- 1) plain 캐시 컬럼 추가 (조회/리스트용)
ALTER TABLE weekly_reports  ADD COLUMN summary varchar(80);
ALTER TABLE monthly_reports ADD COLUMN summary varchar(80);

-- 2) 기존 content jsonb에 summary 키 추가 (없으면 빈 문자열)
UPDATE weekly_reports
SET content = content || jsonb_build_object('summary', COALESCE(summary, ''))
WHERE (content ? 'summary') IS NOT TRUE;

UPDATE monthly_reports
SET content = content || jsonb_build_object('summary', COALESCE(summary, ''))
WHERE (content ? 'summary') IS NOT TRUE;

-- 3) 기존 레코드 summary backfill
UPDATE weekly_reports
SET summary = COALESCE(content->>'summary', '')
WHERE summary IS NULL;

UPDATE monthly_reports
SET summary = COALESCE(content->>'summary', '')
WHERE summary IS NULL;

-- 4) NOT NULL
ALTER TABLE weekly_reports  ALTER COLUMN summary SET NOT NULL;
ALTER TABLE monthly_reports ALTER COLUMN summary SET NOT NULL;