-- V4: Seed categories, suppliers, and products for demonstration

INSERT INTO categories (name) VALUES 
('Beverages'), 
('Snacks'), 
('Dairy'), 
('Bakery'), 
('Household');

INSERT INTO suppliers (supplier_code, supplier_name, mobile, email, gst_number, address, city, state, pincode, active)
VALUES
('SUP001', 'Fresh Foods Ltd', '9876543210', 'fresh@foods.com', 'GST22AABBCCD1Z4', '123 Market Road', 'Los Angeles', 'California', '90001', TRUE),
('SUP002', 'Beverage Wholesalers', '9876543211', 'sales@bevwhole.com', 'GST22AABBCCD2Z5', '456 Bottling St', 'Austin', 'Texas', '73301', TRUE);

INSERT INTO products (name, barcode, sku, category_id, cost_price, selling_price, stock_quantity, reorder_level, is_active)
VALUES
('Coca Cola 500ml', '88880001', 'BEV001', 1, 1.20, 1.80, 50, 10, TRUE),
('Lays Classic 150g', '88880002', 'SNA001', 2, 0.80, 1.50, 40, 10, TRUE),
('Whole Milk 1L', '88880003', 'DAI001', 3, 1.50, 2.20, 30, 5, TRUE),
('Chocolate Chip Cookies', '88880004', 'BAK001', 4, 2.00, 3.50, 25, 5, TRUE),
('Dish Soap 750ml', '88880005', 'HOU001', 5, 1.80, 2.99, 15, 5, TRUE);
