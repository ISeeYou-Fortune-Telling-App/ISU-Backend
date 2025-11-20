-- Add fields for account suspension and ban functionality
ALTER TABLE "user"
ADD COLUMN IF NOT EXISTS suspended_until TIMESTAMP WITHOUT TIME ZONE,
ADD COLUMN IF NOT EXISTS suspension_reason VARCHAR(1000),
ADD COLUMN IF NOT EXISTS is_banned BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS ban_reason VARCHAR(1000),
ADD COLUMN IF NOT EXISTS banned_at TIMESTAMP WITHOUT TIME ZONE,
ADD COLUMN IF NOT EXISTS warning_count INTEGER DEFAULT 0;

-- Add index for better query performance
CREATE INDEX IF NOT EXISTS idx_user_suspended_until ON "user"(suspended_until);
CREATE INDEX IF NOT EXISTS idx_user_is_banned ON "user"(is_banned);

