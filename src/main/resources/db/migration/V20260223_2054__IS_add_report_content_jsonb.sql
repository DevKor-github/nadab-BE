ALTER TABLE weekly_reports ADD COLUMN content jsonb;
ALTER TABLE monthly_reports ADD COLUMN content jsonb;

-- 기존 레코드 backfill (plain -> content)
UPDATE weekly_reports
SET content = jsonb_build_object(
        'discovered', jsonb_build_object(
                'segments', jsonb_build_array(
                        jsonb_build_object('text', COALESCE(discovered, ''), 'marks', jsonb_build_array())
                            )
                      ),
        'improve', jsonb_build_object(
                'segments', jsonb_build_array(
                        jsonb_build_object('text', COALESCE(improve, ''), 'marks', jsonb_build_array())
                            )
                   )
              )
WHERE content IS NULL;

UPDATE monthly_reports
SET content = jsonb_build_object(
        'discovered', jsonb_build_object(
                'segments', jsonb_build_array(
                        jsonb_build_object('text', COALESCE(discovered, ''), 'marks', jsonb_build_array())
                            )
                      ),
        'improve', jsonb_build_object(
                'segments', jsonb_build_array(
                        jsonb_build_object('text', COALESCE(improve, ''), 'marks', jsonb_build_array())
                            )
                   )
              )
WHERE content IS NULL;

ALTER TABLE weekly_reports ALTER COLUMN content SET NOT NULL;
ALTER TABLE monthly_reports ALTER COLUMN content SET NOT NULL;