-- src/main/resources/db/migration/V10__create_payments_table.sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    project_id BIGINT,
    amount BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    vnp_transaction_no VARCHAR(255),
    vnp_bank_code VARCHAR(100),
    vnp_pay_date VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    deleted_at TIMESTAMP WITH TIME ZONE
);

ALTER TABLE payments
    ADD CONSTRAINT fk_payments_user FOREIGN KEY (user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_payments_project FOREIGN KEY (project_id) REFERENCES projects(id);

CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_project_id ON payments(project_id);
CREATE INDEX idx_payments_status ON payments(status);

-- keep updated_at current on updates
CREATE OR REPLACE FUNCTION payments_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_payments_updated_at
BEFORE UPDATE ON payments
FOR EACH ROW
EXECUTE FUNCTION payments_set_updated_at();