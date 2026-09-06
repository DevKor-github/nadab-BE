-- Ask Chat 통계는 사용자·세션을 특정하지 않고 created_at 기간으로 집계한다.
-- 기존 사용자·세션 선두 인덱스와 별도로 전역 기간 조회 경로를 제공한다.
CREATE INDEX idx_ask_chat_sessions_stats_created_id
    ON ask_chat_sessions (created_at DESC, id DESC);

CREATE INDEX idx_ask_chat_messages_stats_created_id
    ON ask_chat_messages (created_at DESC, id DESC);

CREATE INDEX idx_ask_chat_message_references_stats_created_id
    ON ask_chat_message_references (created_at DESC, id DESC);

CREATE INDEX idx_ask_chat_rag_documents_stats_created_id
    ON ask_chat_rag_documents (created_at DESC, id DESC);
