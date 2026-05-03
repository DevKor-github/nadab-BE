ALTER TABLE user_daily_questions
ADD COLUMN reroll_left INT;

UPDATE user_daily_questions
SET reroll_left = 0
WHERE reroll_left IS NULL;

ALTER TABLE user_daily_questions
ALTER COLUMN reroll_left SET DEFAULT 5;

ALTER TABLE user_daily_questions
ALTER COLUMN reroll_left SET NOT NULL;
