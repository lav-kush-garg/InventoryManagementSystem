CREATE DATABASE IF NOT EXISTS inventory_management;
USE inventory_management;
CREATE TABLE IF NOT EXISTS users (
  user_id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(80) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL, role VARCHAR(20) NOT NULL DEFAULT 'ADMIN'
);
CREATE TABLE IF NOT EXISTS suppliers (
  supplier_id INT AUTO_INCREMENT PRIMARY KEY, supplier_name VARCHAR(120) NOT NULL,
  phone VARCHAR(40), email VARCHAR(120), address VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS products (
  product_id INT AUTO_INCREMENT PRIMARY KEY, product_name VARCHAR(120) NOT NULL,
  category VARCHAR(80) NOT NULL, supplier_id INT, quantity INT NOT NULL DEFAULT 0,
  min_stock INT NOT NULL DEFAULT 0, price DECIMAL(12,2) NOT NULL DEFAULT 0,
  CONSTRAINT fk_product_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) ON DELETE SET NULL
);
CREATE TABLE IF NOT EXISTS purchase_orders (
  po_id INT AUTO_INCREMENT PRIMARY KEY, product_id INT NOT NULL, supplier_id INT,
  order_quantity INT NOT NULL, total_amount DECIMAL(12,2) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_po_product FOREIGN KEY (product_id) REFERENCES products(product_id),
  CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
);
-- Additional table required by the specification's stock audit/history feature.
CREATE TABLE IF NOT EXISTS stock_movements (
  movement_id INT AUTO_INCREMENT PRIMARY KEY, product_id INT NOT NULL,
  movement_type VARCHAR(10) NOT NULL, quantity INT NOT NULL, note VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_movement_product FOREIGN KEY (product_id) REFERENCES products(product_id)
);
