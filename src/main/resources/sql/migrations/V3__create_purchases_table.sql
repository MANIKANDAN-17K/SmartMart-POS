-- V3: Create purchases table

CREATE TABLE purchases (
    id INT PRIMARY KEY AUTO_INCREMENT,
    purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    supplier_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
