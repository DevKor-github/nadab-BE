ALTER TABLE ask_chat_rag_documents
    ADD COLUMN retry_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN last_embedding_attempted_at TIMESTAMPTZ;

CREATE INDEX idx_ask_chat_rag_documents_failed_retry
    ON ask_chat_rag_documents(embedding_status, retry_count, created_at)
    WHERE embedding_status = 'FAILED';
