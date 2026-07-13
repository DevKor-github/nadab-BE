CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE ask_chat_rag_documents (
    id                BIGSERIAL    PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    source_type       VARCHAR(32)  NOT NULL,
    source_id         BIGINT       NOT NULL,
    interest_code     VARCHAR(32),
    content           TEXT         NOT NULL,
    metadata          JSONB        NOT NULL DEFAULT '{}'::jsonb,
    embedding         VECTOR(1536),
    embedding_model   VARCHAR(128) NOT NULL,
    embedding_version INTEGER      NOT NULL,
    embedding_status  VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    error_code        VARCHAR(128),
    embedded_at       TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_ask_chat_rag_documents_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_ask_chat_rag_documents_source_version
        UNIQUE (source_type, source_id, embedding_version)
);

CREATE INDEX idx_ask_chat_rag_documents_user_status_created
    ON ask_chat_rag_documents(user_id, embedding_status, created_at DESC);

CREATE INDEX idx_ask_chat_rag_documents_user_interest_created
    ON ask_chat_rag_documents(user_id, interest_code, created_at DESC);

CREATE INDEX idx_ask_chat_rag_documents_source
    ON ask_chat_rag_documents(source_type, source_id);

CREATE INDEX idx_ask_chat_rag_documents_embedding_hnsw
    ON ask_chat_rag_documents
    USING hnsw (embedding vector_cosine_ops)
    WHERE embedding IS NOT NULL;
