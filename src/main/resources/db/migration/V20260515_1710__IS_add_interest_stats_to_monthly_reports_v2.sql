ALTER TABLE monthly_reports_v2
    ADD COLUMN interest_stats JSONB NOT NULL DEFAULT '{"interests":[]}'::jsonb;
