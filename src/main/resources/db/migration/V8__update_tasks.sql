-- V2__update_tasks_to_support_multiple_assignees.sql

-- Step 1: Create the new join table for the many-to-many relationship
CREATE TABLE task_assignees (
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (task_id, user_id)
);

-- Step 2: Add foreign key constraints to the new join table
ALTER TABLE task_assignees
    ADD CONSTRAINT fk_task_assignees_task
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE;

ALTER TABLE task_assignees
    ADD CONSTRAINT fk_task_assignees_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Step 3: Migrate data from the old assignee_id column to the new task_assignees table
-- This assumes the old column was named 'assignee_id'. Adjust if the name was different.
INSERT INTO task_assignees (task_id, user_id)
SELECT id, assignee_id
FROM tasks
WHERE assignee_id IS NOT NULL;

-- Step 4: Drop the old foreign key constraint and the assignee_id column from the tasks table
ALTER TABLE tasks
    DROP CONSTRAINT IF EXISTS fk_task_assignee;

ALTER TABLE tasks
    DROP COLUMN IF EXISTS assignee_id;

-- Step 5: Add indexes to the new table for better query performance
CREATE INDEX idx_task_assignees_task_id ON task_assignees(task_id);
CREATE INDEX idx_task_assignees_user_id ON task_assignees(user_id);