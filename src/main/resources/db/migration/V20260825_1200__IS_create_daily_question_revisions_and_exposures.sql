ALTER TABLE daily_questions
    ADD COLUMN current_revision_no INTEGER NOT NULL DEFAULT 1,
    ADD CONSTRAINT ck_daily_questions_current_revision_no_positive
        CHECK (current_revision_no > 0);

CREATE TABLE daily_question_revisions (
    id BIGSERIAL PRIMARY KEY,
    daily_question_id BIGINT NOT NULL REFERENCES daily_questions(id),
    revision_no INTEGER NOT NULL,
    interest_id INTEGER REFERENCES interests(id),
    question_text VARCHAR(100) NOT NULL,
    question_level INTEGER NOT NULL,
    empathy_guide VARCHAR(100),
    hint_guide VARCHAR(100),
    leading_question_guide VARCHAR(100),
    deleted_at TIMESTAMPTZ,
    effective_from TIMESTAMPTZ NOT NULL,
    source_migration VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_daily_question_revisions_question_revision
        UNIQUE (daily_question_id, revision_no),
    CONSTRAINT uq_daily_question_revisions_id_question
        UNIQUE (id, daily_question_id),
    CONSTRAINT ck_daily_question_revisions_revision_no_positive
        CHECK (revision_no > 0)
);

INSERT INTO daily_question_revisions (
    daily_question_id,
    revision_no,
    interest_id,
    question_text,
    question_level,
    empathy_guide,
    hint_guide,
    leading_question_guide,
    deleted_at,
    effective_from,
    source_migration
)
SELECT
    id,
    current_revision_no,
    interest_id,
    question_text,
    question_level,
    empathy_guide,
    hint_guide,
    leading_question_guide,
    deleted_at,
    NOW(),
    'V20260825_1200'
FROM daily_questions;

CREATE TABLE daily_question_exposures (
    id BIGSERIAL PRIMARY KEY,
    user_daily_question_id BIGINT
        REFERENCES user_daily_questions(id) ON DELETE SET NULL,
    daily_question_revision_id BIGINT NOT NULL
        REFERENCES daily_question_revisions(id),
    assignment_date DATE NOT NULL,
    sequence INTEGER NOT NULL,
    source VARCHAR(30) NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    rerolled_at TIMESTAMPTZ,
    answered_at TIMESTAMPTZ,

    CONSTRAINT uq_daily_question_exposures_assignment_sequence
        UNIQUE (user_daily_question_id, sequence),
    CONSTRAINT ck_daily_question_exposures_sequence_non_negative
        CHECK (sequence >= 0),
    CONSTRAINT ck_daily_question_exposures_source
        CHECK (source IN ('INITIAL', 'REROLL')),
    CONSTRAINT ck_daily_question_exposures_single_terminal_action
        CHECK (rerolled_at IS NULL OR answered_at IS NULL),
    CONSTRAINT ck_daily_question_exposures_rerolled_after_assignment
        CHECK (rerolled_at IS NULL OR rerolled_at >= assigned_at),
    CONSTRAINT ck_daily_question_exposures_answered_after_assignment
        CHECK (answered_at IS NULL OR answered_at >= assigned_at)
);

CREATE INDEX idx_daily_question_exposures_revision_date
    ON daily_question_exposures (daily_question_revision_id, assignment_date);

CREATE UNIQUE INDEX uq_daily_question_exposures_open_assignment
    ON daily_question_exposures (user_daily_question_id)
    WHERE user_daily_question_id IS NOT NULL
      AND rerolled_at IS NULL
      AND answered_at IS NULL;

ALTER TABLE answer_entries
    ADD COLUMN question_revision_id BIGINT,
    ADD CONSTRAINT fk_answer_entries_question_revision
        FOREIGN KEY (question_revision_id, question_id)
            REFERENCES daily_question_revisions(id, daily_question_id);

CREATE INDEX idx_answer_entries_question_revision_id
    ON answer_entries (question_revision_id);
