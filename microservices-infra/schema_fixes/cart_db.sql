ALTER TABLE carts ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE;
ALTER TABLE carts ALTER COLUMN user_id TYPE varchar(36) USING user_id::varchar;
ALTER TABLE cart_items ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE;
