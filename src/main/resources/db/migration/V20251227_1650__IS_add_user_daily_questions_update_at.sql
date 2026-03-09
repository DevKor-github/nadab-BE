ALTER TABLE user_daily_questions
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();