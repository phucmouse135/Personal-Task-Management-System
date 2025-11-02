-- V9__create_chat_messages_table.sql
CREATE TABLE chat_messages (
  id BIGSERIAL PRIMARY KEY,
  sender_id BIGINT NOT NULL,
  receiver_id BIGINT,
  project_id BIGINT,
  content TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

-- Foreign keys (adjust referenced table names if different)
ALTER TABLE chat_messages
  ADD CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE RESTRICT;

ALTER TABLE chat_messages
  ADD CONSTRAINT fk_chat_messages_receiver FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE chat_messages
  ADD CONSTRAINT fk_chat_messages_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL;

-- Indexes
CREATE INDEX idx_chat_messages_sender_id ON chat_messages (sender_id);
CREATE INDEX idx_chat_messages_receiver_id ON chat_messages (receiver_id);
CREATE INDEX idx_chat_messages_project_id ON chat_messages (project_id);