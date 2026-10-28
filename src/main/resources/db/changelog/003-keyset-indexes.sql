--liquibase formatted sql

--changeset aichat:003-keyset-indexes
DROP INDEX ix_chat_user_id;
DROP INDEX ix_chat_user_updated;
CREATE INDEX ix_chat_user_updated ON chat (user_id, updated_at DESC, id DESC);

DROP INDEX ix_message_chat_id;
DROP INDEX ix_message_chat_created;
CREATE INDEX ix_message_chat_created ON message (chat_id, created_at DESC, id DESC);
