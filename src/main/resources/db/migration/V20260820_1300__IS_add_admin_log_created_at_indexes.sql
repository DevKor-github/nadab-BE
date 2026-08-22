CREATE INDEX idx_report_generation_logs_created_id
    ON report_generation_logs(created_at DESC, id DESC);

CREATE INDEX idx_crystal_logs_created_id
    ON crystal_logs(created_at DESC, id DESC);

CREATE INDEX idx_ask_chat_wallet_logs_created_id
    ON ask_chat_wallet_logs(created_at DESC, id DESC);
