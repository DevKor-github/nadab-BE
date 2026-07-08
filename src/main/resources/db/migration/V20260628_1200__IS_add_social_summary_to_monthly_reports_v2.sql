ALTER TABLE monthly_reports_v2
    ADD COLUMN social_summary JSONB NOT NULL DEFAULT '{
      "visible": false,
      "month": 1,
      "likeRanking": [],
      "commentRanking": []
    }'::jsonb;

UPDATE monthly_reports_v2
SET social_summary = jsonb_set(
        social_summary,
        '{month}',
        to_jsonb(EXTRACT(MONTH FROM month_start_date)::int)
                     );

ALTER TABLE monthly_reports_v2
    ALTER COLUMN social_summary DROP DEFAULT;
