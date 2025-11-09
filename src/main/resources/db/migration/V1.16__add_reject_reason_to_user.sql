-- Add reject_reason column to user table
ALTER TABLE "user"
ADD COLUMN reject_reason VARCHAR(1000);

