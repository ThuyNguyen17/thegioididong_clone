-- Migration script to add new columns to products table
-- Execute this in your Laragon MySQL database

USE your_database_name; -- Replace with your actual database name

-- Add new columns to products table
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS image_url VARCHAR(500),
ADD COLUMN IF NOT EXISTS stock INT DEFAULT 0,
ADD COLUMN IF NOT EXISTS rating DECIMAL(3,1) DEFAULT 0.0,
ADD COLUMN IF NOT EXISTS discount INT DEFAULT 0,
ADD COLUMN IF NOT EXISTS brand VARCHAR(100),
ADD COLUMN IF NOT EXISTS discounted_price DECIMAL(12,2) NULL,
ADD COLUMN IF NOT EXISTS promo_stock INT DEFAULT 0,
ADD COLUMN IF NOT EXISTS promo_total INT DEFAULT 0;

-- Insert sample data with new fields
INSERT INTO products (name, price, description, image_url, stock, rating, discount, brand, category_id) 
VALUES 
('iPhone 15 Pro Max', 29990000, 'iPhone 15 Pro Max với chip A17 Pro mạnh mẽ, camera 48MP, màn hình Super Retina XDR 6.7 inch', '📱', 50, 4.8, 10, 'Apple', 1),
('Samsung Galaxy S24 Ultra', 26990000, 'Galaxy S24 Ultra với bút S Pen, camera 200MP, chip Snapdragon 8 Gen 3', '📱', 30, 4.7, 15, 'Samsung', 1),
('MacBook Pro M3', 45990000, 'MacBook Pro 14 inch với chip M3, 16GB RAM, 512GB SSD, màn hình Liquid Retina XDR', '💻', 20, 4.9, 5, 'Apple', 2),
('Dell XPS 15', 35990000, 'Dell XPS 15 với Intel Core i7, 32GB RAM, RTX 4060, màn hình 4K OLED', '💻', 15, 4.6, 12, 'Dell', 2),
('AirPods Pro 2', 5990000, 'AirPods Pro thế hệ 2 với chip H2, chống ồn chủ động, âm thanh không gian', '🎧', 100, 4.6, 20, 'Apple', 3),
('Apple Watch Series 9', 10990000, 'Apple Watch Series 9 với chip S9, màn hình luôn bật, theo dõi sức khỏe toàn diện', '⌚', 60, 4.7, 8, 'Apple', 4),
('iPad Pro M2', 24990000, 'iPad Pro 11 inch với chip M2, màn hình Liquid Retina, hỗ trợ Apple Pencil', '📱', 25, 4.8, 10, 'Apple', 5),
('Sony WH-1000XM5', 8990000, 'Tai nghe chống ồn cao cấp Sony WH-1000XM5, âm thanh Hi-Res, pin 30 giờ', '🎧', 40, 4.8, 15, 'Sony', 3)
ON DUPLICATE KEY UPDATE name=name;

-- Insert sample categories if they don't exist
INSERT INTO categories (name) VALUES 
('Điện thoại'),
('Laptop'),
('Phụ kiện'),
('Đồng hồ'),
('Tablet')
ON DUPLICATE KEY UPDATE name=name;

-- Verify the changes
SELECT * FROM products;
SELECT * FROM categories;
