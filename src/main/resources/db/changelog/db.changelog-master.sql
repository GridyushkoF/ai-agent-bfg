--liquibase formatted sql

--changeset aichat:001-init
CREATE TABLE app_user (
    id UUID NOT NULL,
    login VARCHAR(64) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_app_user PRIMARY KEY (id)
);

CREATE UNIQUE INDEX ux_app_user_login ON app_user (login);
CREATE UNIQUE INDEX ux_app_user_email ON app_user (email);

CREATE TABLE chat (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    title VARCHAR(120),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_chat PRIMARY KEY (id),
    CONSTRAINT fk_chat_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE INDEX ix_chat_user_id ON chat (user_id);
CREATE INDEX ix_chat_user_updated ON chat (user_id, updated_at);

CREATE TABLE message (
    id UUID NOT NULL,
    chat_id UUID NOT NULL,
    role VARCHAR(16) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_message PRIMARY KEY (id),
    CONSTRAINT fk_message_chat FOREIGN KEY (chat_id) REFERENCES chat (id),
    CONSTRAINT ck_message_role CHECK (role IN ('USER', 'ASSISTANT'))
);

CREATE INDEX ix_message_chat_id ON message (chat_id);
CREATE INDEX ix_message_chat_created ON message (chat_id, created_at);
