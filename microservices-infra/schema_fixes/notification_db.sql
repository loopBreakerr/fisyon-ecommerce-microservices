ALTER TABLE notifications ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE;
ALTER TABLE notifications ALTER COLUMN user_id TYPE varchar(36) USING user_id::varchar;
ALTER TABLE notification_templates ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE;
