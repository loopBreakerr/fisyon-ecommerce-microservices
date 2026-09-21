ALTER TABLE order_items ADD COLUMN seller_id VARCHAR(36);
ALTER TABLE order_items ADD COLUMN product_name VARCHAR(255);
ALTER TABLE order_items ADD COLUMN product_image_id BIGINT;
