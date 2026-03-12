ALTER TABLE answer_entries
DROP CONSTRAINT answer_entries_user_id_fkey;

ALTER TABLE answer_entries
    ADD CONSTRAINT answer_entries_user_id_fkey
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE;


ALTER TABLE daily_reports
DROP CONSTRAINT fk_daily_reports_answer_entry;

ALTER TABLE daily_reports
    ADD CONSTRAINT fk_daily_reports_answer_entry
        FOREIGN KEY (answer_entry_id)
            REFERENCES answer_entries(id)
            ON DELETE CASCADE;


ALTER TABLE monthly_reports
DROP CONSTRAINT fk_monthly_reports_user;

ALTER TABLE monthly_reports
    ADD CONSTRAINT fk_monthly_reports_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE;


ALTER TABLE weekly_reports
DROP CONSTRAINT fk_weekly_reports_user_id;

ALTER TABLE weekly_reports
    ADD CONSTRAINT fk_weekly_reports_user_id
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE;


ALTER TABLE user_daily_questions
DROP CONSTRAINT user_daily_questions_user_id_fkey;

ALTER TABLE user_daily_questions
    ADD CONSTRAINT user_daily_questions_user_id_fkey
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE;