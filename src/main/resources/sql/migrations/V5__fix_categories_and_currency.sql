-- V5: Fix categories table - add missing columns required by CategoryDao
-- Also update currency symbol from $ to ₹ in all settings tables

-- Add missing columns to categories table
ALTER TABLE categories ADD COLUMN IF NOT EXISTS description VARCHAR(255) DEFAULT '' AFTER name;
ALTER TABLE categories ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE AFTER description;
ALTER TABLE categories ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP AFTER active;
ALTER TABLE categories ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

-- Update currency symbol from $ to ₹ in store_settings
UPDATE store_settings SET currency_symbol = '₹' WHERE currency_symbol = '$';

-- Update currency symbol from $ to ₹ in settings table
UPDATE settings SET currency_symbol = '₹' WHERE currency_symbol = '$';
