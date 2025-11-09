-- Flyway migration: V1.17
-- Normalize message_type values in `message` table to valid enum values.
-- Set NULL or any value not 'USER' or 'SYSTEM' (case-insensitive) to 'USER'.

-- This migration is idempotent: running it multiple times has no further effect.

UPDATE message
SET message_type = 'USER'
WHERE message_type IS NULL
   OR UPPER(message_type) NOT IN ('USER', 'SYSTEM');

-- If you want to map specific legacy values explicitly, add UPDATE statements below, e.g.:
-- UPDATE message SET message_type = 'USER' WHERE UPPER(message_type) = 'TEXT';
-- UPDATE message SET message_type = 'SYSTEM' WHERE UPPER(message_type) = 'SYS';

