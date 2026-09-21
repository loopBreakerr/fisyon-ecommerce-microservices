ALTER TABLE payments ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE;
ALTER TABLE payment_transactions ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE;
