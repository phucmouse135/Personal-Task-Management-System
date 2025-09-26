-- File: V2__create_roles_table.sql
-- Description: Create the roles table

CREATE TABLE IF NOT EXISTS roles (
                                     name VARCHAR(50) PRIMARY KEY, -- Unique name of the role
    description VARCHAR(255), -- Description of the role,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- Creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- Last update timestamp
    deleted_at TIMESTAMP -- Deletion timestamp
    );