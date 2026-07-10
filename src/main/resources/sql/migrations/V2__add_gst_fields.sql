-- V2: Add GST fields, barcode, stock quantity renaming, customer columns, and settings/tax tables

-- Update products table
ALTER TABLE products ADD COLUMN barcode VARCHAR(50) UNIQUE AFTER name;
ALTER TABLE products ADD COLUMN gst_percent DECIMAL(5,2) DEFAULT 0 AFTER selling_price;
ALTER TABLE products ADD COLUMN image_path VARCHAR(255) AFTER is_active;
ALTER TABLE products ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE' AFTER image_path;
ALTER TABLE products ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;
ALTER TABLE products RENAME COLUMN quantity_in_stock TO stock_quantity;

-- Update customers table
ALTER TABLE customers ADD COLUMN address VARCHAR(255) AFTER email;
ALTER TABLE customers ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE' AFTER address;
ALTER TABLE customers ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

-- Create settings table to replace store_settings if needed, aligned with SettingsDao
CREATE TABLE settings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    store_name VARCHAR(150) NOT NULL DEFAULT 'SmartMart POS',
    address VARCHAR(255) DEFAULT '',
    gst_number VARCHAR(20) DEFAULT '',
    phone VARCHAR(20) DEFAULT '',
    email VARCHAR(150) DEFAULT '',
    logo_path VARCHAR(255) DEFAULT '',
    receipt_header TEXT,
    receipt_footer TEXT,
    show_logo_on_receipt BOOLEAN DEFAULT FALSE,
    show_gst_on_receipt BOOLEAN DEFAULT FALSE,
    show_cashier_on_receipt BOOLEAN DEFAULT FALSE,
    theme VARCHAR(20) NOT NULL DEFAULT 'light',
    app_version VARCHAR(20) NOT NULL DEFAULT '1.0.0',
    currency_symbol VARCHAR(5) NOT NULL DEFAULT '$',
    tax_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00
);

-- Insert initial row in settings
INSERT INTO settings (id, store_name, currency_symbol, theme, app_version)
VALUES (1, 'SmartMart POS', '$', 'light', '1.0.0');

-- Create tax_settings table
CREATE TABLE tax_settings (
    id INT PRIMARY KEY,
    gst_percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    gst_enabled BOOLEAN NOT NULL DEFAULT FALSE
);

-- Insert initial row in tax_settings
INSERT INTO tax_settings (id, gst_percentage, gst_enabled)
VALUES (1, 0.00, FALSE);

-- Update suppliers table
ALTER TABLE suppliers RENAME COLUMN name TO supplier_name;
ALTER TABLE suppliers RENAME COLUMN phone TO mobile;
ALTER TABLE suppliers RENAME COLUMN is_active TO active;
ALTER TABLE suppliers ADD COLUMN supplier_code VARCHAR(50) UNIQUE AFTER id;
ALTER TABLE suppliers ADD COLUMN contact_person VARCHAR(100) AFTER supplier_name;
ALTER TABLE suppliers ADD COLUMN gst_number VARCHAR(20) AFTER email;
ALTER TABLE suppliers ADD COLUMN address VARCHAR(255) AFTER gst_number;
ALTER TABLE suppliers ADD COLUMN city VARCHAR(100) AFTER address;
ALTER TABLE suppliers ADD COLUMN state VARCHAR(100) AFTER city;
ALTER TABLE suppliers ADD COLUMN pincode VARCHAR(20) AFTER state;
ALTER TABLE suppliers ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

-- Recreate bills table to match model mapping
DROP TABLE IF EXISTS bill_items;
DROP TABLE IF EXISTS bills;

CREATE TABLE bills (
    id INT PRIMARY KEY AUTO_INCREMENT,
    invoice_number VARCHAR(30) UNIQUE NOT NULL,
    customer_id INT NULL,
    cashier_id INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    gst_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    grand_total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_method VARCHAR(20) NOT NULL DEFAULT 'CASH',
    cash_paid DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    card_paid DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    upi_paid DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    amount_paid DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (cashier_id) REFERENCES users(id)
);

CREATE TABLE bill_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    bill_id INT NOT NULL,
    product_id INT NOT NULL,
    barcode VARCHAR(50),
    product_name VARCHAR(150) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    discount_percent DECIMAL(5,2) DEFAULT 0.00,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    gst_percent DECIMAL(5,2) DEFAULT 0.00,
    gst_amount DECIMAL(10,2) DEFAULT 0.00,
    line_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (bill_id) REFERENCES bills(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
