ALTER TABLE categories ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE;
ALTER TABLE products ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE;
ALTER TABLE product_images ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE;
ALTER TABLE products ADD COLUMN seller_id VARCHAR(36);
