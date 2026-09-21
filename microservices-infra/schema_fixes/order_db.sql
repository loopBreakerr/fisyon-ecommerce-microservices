ALTER TABLE orders ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE;
ALTER TABLE orders ALTER COLUMN user_id TYPE varchar(36) USING user_id::varchar;
ALTER TABLE order_items ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE;
ALTER TABLE order_status_history ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE;
