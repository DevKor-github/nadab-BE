CREATE INDEX idx_type_reports_completed_date_interest
    ON type_reports (date, interest_code)
    WHERE status = 'COMPLETED';
