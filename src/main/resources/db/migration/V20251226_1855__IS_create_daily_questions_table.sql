CREATE TABLE daily_questions (
                                 id BIGSERIAL PRIMARY KEY,

                                 interest_id INTEGER REFERENCES interests(id),

                                 question_text VARCHAR(100) NOT NULL,

                                 question_level INTEGER NOT NULL,

                                 empathy_guide VARCHAR(100),
                                 hint_guide VARCHAR(100),
                                 leading_question_guide VARCHAR(100),

                                 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 deleted_at TIMESTAMPTZ
);
-- ON DELETE CASCADE 미설정 이유 : daily_questions 항목이 삭제되더라도 기존에 해당 질문에 답변한 기록은 보존되어야 함.
