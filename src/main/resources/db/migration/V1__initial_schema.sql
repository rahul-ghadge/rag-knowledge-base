-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Document table: stores uploaded documents metadata
CREATE TABLE IF NOT EXISTS documents (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name            VARCHAR(500)  NOT NULL,
    original_name   VARCHAR(500)  NOT NULL,
    content_type    VARCHAR(100)  NOT NULL,
    file_size       BIGINT        NOT NULL,
    status          VARCHAR(50)   NOT NULL DEFAULT 'PROCESSING',
    tags            TEXT[],
    description     TEXT,
    storage_path    VARCHAR(1000),
    chunk_count     INTEGER       DEFAULT 0,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255)  NOT NULL DEFAULT 'system',
    updated_by      VARCHAR(255)  NOT NULL DEFAULT 'system',
    is_deleted      BOOLEAN       NOT NULL DEFAULT FALSE
);

-- Document chunks: stores text chunks with embeddings
CREATE TABLE IF NOT EXISTS document_chunks (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    document_id     UUID          NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    chunk_index     INTEGER       NOT NULL,
    content         TEXT          NOT NULL,
    token_count     INTEGER,
    embedding       vector(1536),
    metadata        JSONB,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Spring AI vector store table (used by Spring AI PgVectorStore)
CREATE TABLE IF NOT EXISTS vector_store (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    content         TEXT,
    metadata        JSONB,
    embedding       vector(1536)
);

-- Chat sessions: tracks conversation history
CREATE TABLE IF NOT EXISTS chat_sessions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_name    VARCHAR(255),
    user_identifier VARCHAR(255),
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active       BOOLEAN       NOT NULL DEFAULT TRUE
);

-- Chat messages: individual messages in a session
CREATE TABLE IF NOT EXISTS chat_messages (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id      UUID          NOT NULL REFERENCES chat_sessions(id) ON DELETE CASCADE,
    role            VARCHAR(50)   NOT NULL,
    content         TEXT          NOT NULL,
    tokens_used     INTEGER,
    model_used      VARCHAR(100),
    sources         JSONB,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_documents_status          ON documents(status);
CREATE INDEX IF NOT EXISTS idx_documents_created_at      ON documents(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_documents_is_deleted      ON documents(is_deleted);
CREATE INDEX IF NOT EXISTS idx_chunks_document_id        ON document_chunks(document_id);
CREATE INDEX IF NOT EXISTS idx_chunks_embedding          ON document_chunks USING hnsw (embedding vector_cosine_ops);
CREATE INDEX IF NOT EXISTS idx_vector_store_embedding    ON vector_store USING hnsw (embedding vector_cosine_ops);
CREATE INDEX IF NOT EXISTS idx_chat_messages_session_id  ON chat_messages(session_id);
CREATE INDEX IF NOT EXISTS idx_chat_sessions_user        ON chat_sessions(user_identifier);

-- Trigger: auto-update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_documents_updated_at
    BEFORE UPDATE ON documents
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_chat_sessions_updated_at
    BEFORE UPDATE ON chat_sessions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
