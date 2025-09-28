CREATE TABLE projects (
    id integer PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    owner_id integer REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- Creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- Last update timestamp
    deleted_at TIMESTAMP -- Deletion timestamp
);