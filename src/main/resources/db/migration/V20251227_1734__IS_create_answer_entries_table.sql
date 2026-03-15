CREATE TABLE answer_entries (
                    id BIGSERIAL PRIMARY KEY,

                    user_id BIGINT NOT NULL REFERENCES users(id),
                    question_id BIGINT NOT NULL REFERENCES daily_questions(id),

                    content VARCHAR(500) NOT NULL,

                    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_answer_entries_user_id ON answer_entries(user_id);
CREATE INDEX idx_answer_entries_question_id ON answer_entries(question_id);
