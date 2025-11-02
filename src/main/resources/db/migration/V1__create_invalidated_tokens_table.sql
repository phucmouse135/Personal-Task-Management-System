-- File: V1__create_invalidated_tokens_table.sql
-- Description: Create the invalidated_tokens table

CREATE TABLE IF NOT EXISTS invalidated_tokens (
                                                  id VARCHAR(255) PRIMARY KEY, -- Unique identifier of the invalidated token
    expiry_time TIMESTAMP NOT NULL UNIQUE ,-- Expiry time of the invalidated token
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- Creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- Last update timestamp
    deleted_at TIMESTAMP -- Deletion timestamp
    );