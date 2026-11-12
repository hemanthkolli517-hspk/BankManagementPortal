USE bank_management;

-- Run this migration once against an existing Bankwise database.
-- Existing employees are marked APPROVED so their current access is preserved.
ALTER TABLE employees
  ADD COLUMN application_status ENUM('PENDING','APPROVED','REJECTED')
  NOT NULL DEFAULT 'PENDING' AFTER status;

UPDATE employees SET application_status='APPROVED';

CREATE TABLE IF NOT EXISTS audit_log (
  audit_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NULL,
  actor_username VARCHAR(50) NOT NULL,
  actor_role VARCHAR(30) NOT NULL,
  http_method VARCHAR(10) NOT NULL,
  request_path VARCHAR(255) NOT NULL,
  action_detail VARCHAR(255) NULL,
  ip_address VARCHAR(50) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_audit_actor_date(actor_username, created_at),
  INDEX idx_audit_date(created_at)
);
