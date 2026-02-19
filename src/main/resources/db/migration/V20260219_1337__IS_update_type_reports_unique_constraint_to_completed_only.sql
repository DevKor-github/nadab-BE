DROP INDEX IF EXISTS uq_type_reports_user_interest_active;

CREATE UNIQUE INDEX uq_type_reports_user_interest_active_completed
    ON type_reports (user_id, interest_code)
    WHERE deleted_at IS NULL AND status = 'COMPLETED';