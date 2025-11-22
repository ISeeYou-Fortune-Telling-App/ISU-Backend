-- Flyway migration: V1.24
-- Fix message status values to match enum constant names
-- Change 'SENT' to 'UNREAD' to align with MessageStatusEnum.UNREAD

-- This migration is idempotent: running it multiple times has no further effect.

UPDATE message
SET status = 'UNREAD'
WHERE UPPER(status) = 'SENT';

-- Ensure all status values are valid enum constants
UPDATE message
SET status = 'UNREAD'
WHERE status IS NULL
   OR UPPER(status) NOT IN ('UNREAD', 'READ', 'DELETED', 'REMOVED');

