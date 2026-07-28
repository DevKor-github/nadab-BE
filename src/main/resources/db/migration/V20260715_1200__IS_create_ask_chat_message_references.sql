CREATE TABLE ask_chat_message_references (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    rag_document_id BIGINT NOT NULL,
    display_order INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ask_chat_message_references_message
        FOREIGN KEY (message_id) REFERENCES ask_chat_messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_ask_chat_message_references_rag_document
        FOREIGN KEY (rag_document_id) REFERENCES ask_chat_rag_documents(id) ON DELETE RESTRICT,
    CONSTRAINT uq_ask_chat_message_references_message_document
        UNIQUE (message_id, rag_document_id),
    CONSTRAINT uq_ask_chat_message_references_message_order
        UNIQUE (message_id, display_order)
);

CREATE INDEX idx_ask_chat_message_references_message_order
    ON ask_chat_message_references(message_id, display_order);

CREATE INDEX idx_ask_chat_message_references_rag_document
    ON ask_chat_message_references(rag_document_id);
