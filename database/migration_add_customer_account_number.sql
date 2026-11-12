-- Run this only if bank_management already exists and customers.account_number
-- has not been added yet. Existing data is preserved.
USE bank_management;

ALTER TABLE customers
    ADD COLUMN account_number VARCHAR(20) NULL UNIQUE;
