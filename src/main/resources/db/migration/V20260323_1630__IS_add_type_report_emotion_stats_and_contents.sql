ALTER TABLE type_reports
    ADD COLUMN type_analysis_content jsonb,
    ADD COLUMN emotion_summary_content jsonb,
    ADD COLUMN emotion_stats jsonb;

-- 기존 레코드 backfill (plain -> content)
UPDATE type_reports
SET type_analysis_content = jsonb_build_object(
        'styledText', jsonb_build_object(
                'segments', jsonb_build_array(
                        jsonb_build_object('text', COALESCE(type_analysis, ''), 'marks', jsonb_build_array())
                            )
                      )
    )
WHERE type_analysis_content IS NULL;

UPDATE type_reports
SET emotion_summary_content = jsonb_build_object(
        'styledText', jsonb_build_object(
                'segments', jsonb_build_array()
                      )
    )
WHERE emotion_summary_content IS NULL;

UPDATE type_reports
SET emotion_stats = jsonb_build_object(
        'totalCount', 0,
        'dominantEmotionCode', null,
        'positivePercent', 0,
        'emotions', jsonb_build_array()
    )
WHERE emotion_stats IS NULL;

ALTER TABLE type_reports ALTER COLUMN type_analysis_content SET NOT NULL;
ALTER TABLE type_reports ALTER COLUMN emotion_summary_content SET NOT NULL;
ALTER TABLE type_reports ALTER COLUMN emotion_stats SET NOT NULL;
