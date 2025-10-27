-- Migration script: Allow null sender for system messages
-- Date: 2025-10-28

-- Step 1: Update existing messages with null sender to have a system user (optional)
-- If you want to keep data integrity, uncomment and run this first:
-- UPDATE message SET sender_id = (SELECT user_id FROM "user" WHERE role = 'ADMIN' LIMIT 1) WHERE sender_id IS NULL;

-- Step 2: Alter column to allow NULL
ALTER TABLE message ALTER COLUMN sender_id DROP NOT NULL;

-- Step 3: Add comment to explain nullable sender
COMMENT ON COLUMN message.sender_id IS 'User who sent the message. NULL for system messages.';

-- Verify the change
SELECT
    column_name,
    is_nullable,
    data_type
FROM information_schema.columns
WHERE table_name = 'message'
  AND column_name = 'sender_id';

