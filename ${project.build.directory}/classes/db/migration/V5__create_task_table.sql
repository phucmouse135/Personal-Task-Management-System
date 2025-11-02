CREATE TABLE IF NOT EXISTS tasks (
                       id BIGSERIAL PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       description TEXT,

    -- Sử dụng kiểu VARCHAR cho ENUM, hoặc dùng kiểu ENUM của Postgres
                       status VARCHAR(50) NOT NULL DEFAULT 'TODO',
                       priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',

                       deadline TIMESTAMP,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        deleted_at TIMESTAMP,

                       project_id BIGINT NOT NULL,
                       assignee_id BIGINT,

    -- Khóa ngoại trỏ đến dự án
                       CONSTRAINT fk_task_project
                           FOREIGN KEY(project_id)
                               REFERENCES projects(id)
                               ON DELETE CASCADE, -- Nếu xóa project thì xóa luôn task

    -- Khóa ngoại trỏ đến người được gán
                       CONSTRAINT fk_task_assignee
                           FOREIGN KEY(assignee_id)
                               REFERENCES users(id)
                               ON DELETE SET NULL -- Nếu xóa user, task này trở nên "vô chủ"
);