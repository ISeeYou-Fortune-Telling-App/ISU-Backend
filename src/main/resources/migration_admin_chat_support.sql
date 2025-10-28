-- Migration: Add admin chat support
-- Date: 2025-10-28
-- Description: Allow admin to create conversations with any user (customer or seer)

-- 1. Make booking_id nullable (for admin chats)
ALTER TABLE conversation
    MODIFY COLUMN booking_id BINARY(16) NULL;

-- 2. Add admin_id column
ALTER TABLE conversation
    ADD COLUMN admin_id BINARY(16) NULL AFTER booking_id,
    ADD CONSTRAINT fk_conversation_admin
        FOREIGN KEY (admin_id) REFERENCES user(user_id)
        ON DELETE CASCADE;

-- 3. Add target_user_id column
ALTER TABLE conversation
    ADD COLUMN target_user_id BINARY(16) NULL AFTER admin_id,
    ADD CONSTRAINT fk_conversation_target_user
        FOREIGN KEY (target_user_id) REFERENCES user(user_id)
        ON DELETE CASCADE;

-- 4. Add index for better query performance
CREATE INDEX idx_conversation_admin ON conversation(admin_id);
CREATE INDEX idx_conversation_target_user ON conversation(target_user_id);
CREATE INDEX idx_conversation_type ON conversation(type);

-- 5. Add check constraint (either booking_id exists OR (admin_id AND target_user_id exist))
-- Note: MySQL doesn't support CHECK constraints until 8.0.16, so this is optional
-- If using MySQL 8.0.16+, uncomment the following:
/*
ALTER TABLE conversation
    ADD CONSTRAINT chk_conversation_participants
    CHECK (
        (booking_id IS NOT NULL AND admin_id IS NULL AND target_user_id IS NULL) OR
        (booking_id IS NULL AND admin_id IS NOT NULL AND target_user_id IS NOT NULL)
    );
*/

-- 6. Update existing conversations to ensure data consistency
-- (All existing conversations should have booking_id, so no updates needed)

COMMIT;

