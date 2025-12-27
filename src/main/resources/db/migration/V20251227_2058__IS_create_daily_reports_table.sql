CREATE TABLE daily_reports (
                               id              BIGSERIAL PRIMARY KEY,
                               answer_entry_id BIGINT      NOT NULL,
                               emotion_id      INT,
                               content         VARCHAR(500),
                               analyzed_at     TIMESTAMPTZ,
                               created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

                               CONSTRAINT fk_daily_reports_answer_entry
                                   FOREIGN KEY (answer_entry_id) REFERENCES answer_entries(id),

                               CONSTRAINT fk_daily_reports_emotion
                                   FOREIGN KEY (emotion_id) REFERENCES emotions(id)
);
