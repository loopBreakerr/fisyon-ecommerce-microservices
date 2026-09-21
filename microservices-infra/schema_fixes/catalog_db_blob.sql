ALTER TABLE product_images ADD COLUMN image_data BYTEA;
ALTER TABLE product_images ADD COLUMN content_type VARCHAR(100);
ALTER TABLE product_images DROP COLUMN image_url;
