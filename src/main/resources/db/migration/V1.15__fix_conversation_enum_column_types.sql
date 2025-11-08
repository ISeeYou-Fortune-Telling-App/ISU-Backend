-- =============================================
-- Fortune Telling Application - Database Schema
-- Version: 1.15
-- Description: Change conversation status and type from VARCHAR to SMALLINT to match enum ordinal mapping
-- =============================================

-- Step 1: Update existing VARCHAR values to their corresponding ordinal positions
UPDATE conversation
SET status = CASE
    WHEN status = 'WAITING' THEN '0'
    WHEN status = 'ACTIVE' THEN '1'
    WHEN status = 'ENDED' THEN '2'
    WHEN status = 'CANCELLED' THEN '3'
    ELSE status  -- Keep numeric values as is
END
WHERE status IN ('WAITING', 'ACTIVE', 'ENDED', 'CANCELLED', '0', '1', '2', '3');

UPDATE conversation
SET type = CASE
    WHEN type = 'BOOKING_SESSION' THEN '0'
    WHEN type = 'SUPPORT' THEN '1'
    WHEN type = 'ADMIN_CHAT' THEN '2'
    ELSE type  -- Keep numeric values as is
END
WHERE type IN ('BOOKING_SESSION', 'SUPPORT', 'ADMIN_CHAT', '0', '1', '2');

-- Step 2: Alter column types from VARCHAR to SMALLINT
ALTER TABLE conversation
ALTER COLUMN status TYPE SMALLINT USING status::SMALLINT;

ALTER TABLE conversation
ALTER COLUMN type TYPE SMALLINT USING type::SMALLINT;

