ALTER TABLE reviews ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE;
ALTER TABLE reviews ALTER COLUMN user_id TYPE varchar(36) USING user_id::varchar;
ALTER TABLE review_votes ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE;
ALTER TABLE review_votes ALTER COLUMN user_id TYPE varchar(36) USING user_id::varchar;
