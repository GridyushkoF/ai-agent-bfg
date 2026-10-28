--liquibase formatted sql

--changeset aichat:002-chat-title-trgm
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX ix_chat_title_trgm ON chat USING gin (lower(title) gin_trgm_ops);
