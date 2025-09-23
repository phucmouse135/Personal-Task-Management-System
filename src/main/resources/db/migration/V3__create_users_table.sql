-- File: V3__create_users_table.sql
-- Description: Create the users table

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY, -- Unique identifier of the user
    username VARCHAR(50) NOT NULL UNIQUE, -- Unique username of the user
    password VARCHAR(100) NOT NULL, -- Password of the user
    first_name VARCHAR(100), -- First name of the user
    last_name VARCHAR(100), -- Last name of the user
    email VARCHAR(255) NOT NULL UNIQUE, -- Email address of the user
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- Creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- Last update timestamp
    deleted_at TIMESTAMP -- Deletion timestamp
);

CREATE TABLE IF NOT EXISTS users_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE, -- Foreign key to users table
    role_name VARCHAR(50) NOT NULL REFERENCES roles(name) ON DELETE CASCADE, -- Foreign key to roles table
    PRIMARY KEY (user_id, role_name) -- Composite primary key
);