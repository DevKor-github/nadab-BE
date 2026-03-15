CREATE TABLE user_daily_questions (
                        id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT NOT NULL REFERENCES users(id),
                        date DATE NOT NULL,              -- KST 기준 오늘
                        daily_question_id BIGINT NOT NULL REFERENCES daily_questions(id),
                        reroll_used BOOLEAN NOT NULL DEFAULT FALSE,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                        UNIQUE (user_id, date)
);
