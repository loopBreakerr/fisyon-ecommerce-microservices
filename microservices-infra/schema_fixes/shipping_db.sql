ALTER TABLE shipments ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE;
ALTER TABLE shipping_addresses ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE;
