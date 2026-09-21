ALTER TABLE inventory_items ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE;
ALTER TABLE inventory_movements ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE;
