ALTER TABLE recommendations ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE;
ALTER TABLE recommendations ALTER COLUMN user_id TYPE varchar(36) USING user_id::varchar;
ALTER TABLE user_interactions ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE;
ALTER TABLE user_interactions ALTER COLUMN user_id TYPE varchar(36) USING user_id::varchar;
