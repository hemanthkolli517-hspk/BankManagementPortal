CREATE DATABASE IF NOT EXISTS bank_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bank_management;

CREATE TABLE roles (
  role_id INT PRIMARY KEY AUTO_INCREMENT,
  role_name VARCHAR(30) NOT NULL UNIQUE
);
INSERT IGNORE INTO roles(role_id, role_name) VALUES (1,'ADMIN'),(2,'EMPLOYEE'),(3,'CUSTOMER');

CREATE TABLE users (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  mobile VARCHAR(15) NOT NULL,
  role_id INT NOT NULL,
  status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_users_role FOREIGN KEY(role_id) REFERENCES roles(role_id)
);

CREATE TABLE customers (
  customer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  full_name VARCHAR(100) NOT NULL,
  gender ENUM('Male','Female','Other') NOT NULL,
  dob DATE NULL,
  address TEXT NOT NULL,
  identity_type VARCHAR(30) NOT NULL,
  identity_file_name VARCHAR(255) NULL,
  account_number VARCHAR(20) NULL UNIQUE,
  account_status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_customer_user FOREIGN KEY(user_id) REFERENCES users(user_id)
);

CREATE TABLE employees (
  employee_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  employee_code VARCHAR(20) NOT NULL UNIQUE,
  designation VARCHAR(50) NOT NULL,
  department VARCHAR(50) NOT NULL,
  address TEXT,
  status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  application_status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_employee_user FOREIGN KEY(user_id) REFERENCES users(user_id)
);


CREATE TABLE user_sessions (
  session_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  logout_time TIMESTAMP NULL,
  ip_address VARCHAR(50),
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_session_user FOREIGN KEY(user_id) REFERENCES users(user_id)
);

CREATE TABLE audit_log (
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

CREATE TABLE accounts (
  account_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  customer_id BIGINT NOT NULL,
  account_number VARCHAR(20) NOT NULL UNIQUE,
  account_type ENUM('SAVINGS','CURRENT') NOT NULL DEFAULT 'SAVINGS',
  balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_account_customer FOREIGN KEY(customer_id) REFERENCES customers(customer_id),
  CONSTRAINT chk_balance_nonnegative CHECK(balance >= 0)
);

CREATE TABLE transactions (
  transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  account_id BIGINT NOT NULL,
  transaction_type ENUM('DEPOSIT','WITHDRAWAL') NOT NULL,
  amount DECIMAL(15,2) NOT NULL,
  balance_after DECIMAL(15,2) NOT NULL,
  transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  description VARCHAR(255),
  performed_by BIGINT NULL,
  CONSTRAINT fk_tx_account FOREIGN KEY(account_id) REFERENCES accounts(account_id),
  CONSTRAINT fk_tx_user FOREIGN KEY(performed_by) REFERENCES users(user_id),
  CONSTRAINT chk_tx_amount CHECK(amount > 0),
  INDEX idx_tx_account_date(account_id,transaction_date)
);

show tables;
select * from customers;
