ALTER TABLE report_generation_logs
    ADD COLUMN input_tokens BIGINT,
    ADD COLUMN output_tokens BIGINT,
    ADD COLUMN total_tokens BIGINT;
