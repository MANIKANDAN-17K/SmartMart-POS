CREATE TABLE users (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password_hash VARCHAR(100) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       is_active BOOLEAN NOT NULL DEFAULT TRUE,
                       last_login TIMESTAMP NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
                            id INT PRIMARY KEY AUTO_INCREMENT,
                            name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE products (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          name VARCHAR(150) NOT NULL,
                          sku VARCHAR(50) UNIQUE,
                          category_id INT NULL,
                          cost_price DECIMAL(10,2) NOT NULL DEFAULT 0,
                          selling_price DECIMAL(10,2) NOT NULL DEFAULT 0,
                          quantity_in_stock INT NOT NULL DEFAULT 0,
                          reorder_level INT NOT NULL DEFAULT 5,
                          is_active BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE customers (
                           id INT PRIMARY KEY AUTO_INCREMENT,
                           name VARCHAR(150) NOT NULL,
                           phone VARCHAR(20),
                           email VARCHAR(150),
                           is_active BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE suppliers (
                           id INT PRIMARY KEY AUTO_INCREMENT,
                           name VARCHAR(150) NOT NULL,
                           phone VARCHAR(20),
                           email VARCHAR(150),
                           is_active BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bills (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       invoice_number VARCHAR(30) UNIQUE NOT NULL,
                       customer_id INT NULL,
                       created_by INT NOT NULL,
                       subtotal DECIMAL(10,2) NOT NULL DEFAULT 0,
                       tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
                       discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
                       total_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
                       profit_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
                       payment_type VARCHAR(20) NOT NULL DEFAULT 'CASH',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (customer_id) REFERENCES customers(id),
                       FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE bill_items (
                            id INT PRIMARY KEY AUTO_INCREMENT,
                            bill_id INT NOT NULL,
                            product_id INT NOT NULL,
                            quantity INT NOT NULL,
                            unit_price DECIMAL(10,2) NOT NULL,
                            cost_price DECIMAL(10,2) NOT NULL,
                            line_total DECIMAL(10,2) NOT NULL,
                            FOREIGN KEY (bill_id) REFERENCES bills(id),
                            FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE store_settings (
                                id INT PRIMARY KEY AUTO_INCREMENT,
                                store_name VARCHAR(150) NOT NULL DEFAULT 'SmartMart POS',
                                currency_symbol VARCHAR(5) NOT NULL DEFAULT '$',
                                tax_rate DECIMAL(5,2) NOT NULL DEFAULT 0,
                                theme VARCHAR(20) NOT NULL DEFAULT 'light',
                                app_version VARCHAR(20) NOT NULL DEFAULT '1.0.0'
);

INSERT INTO store_settings (store_name, currency_symbol, tax_rate, theme, app_version)
VALUES ('SmartMart POS', '$', 0.00, 'light', '1.0.0');

-- Replace the hash below with a real BCrypt hash of Admin@123 before running
-- (see the jbcrypt scratch snippet from earlier in this conversation)
INSERT INTO users (username, password_hash, role, is_active)
VALUES ('admin', '$2a$12$ruZaug.65sc79Rs4ynaO0eStx3FzEkWhJuHuUpGt5/mPQv6v66Km.', 'ADMIN', TRUE);
INSERT INTO users (username, password_hash, role, is_active)
VALUES ('cashier', '$2a$12$GWtJoAnXPBeUczw3YlsmeusDdfKtVJt3YCjNyqiN9cmBp78ZDizii', 'CASHIER', TRUE);