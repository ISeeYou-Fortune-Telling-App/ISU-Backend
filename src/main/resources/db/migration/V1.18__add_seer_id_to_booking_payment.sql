-- Add seer_id column to booking_payment table
ALTER TABLE booking_payment
ADD COLUMN seer_id UUID;

-- Make booking_id nullable (for BONUS payments)
ALTER TABLE booking_payment
ALTER COLUMN booking_id DROP NOT NULL;

-- Add foreign key constraint for seer_id
ALTER TABLE booking_payment
ADD CONSTRAINT FK_BOOKING_PAYMENT_ON_SEER FOREIGN KEY (seer_id) REFERENCES "user" (user_id);

-- Add comment for clarity
COMMENT ON COLUMN booking_payment.seer_id IS 'Seer ID - used for BONUS payment type to identify the seer receiving the bonus';
COMMENT ON COLUMN booking_payment.booking_id IS 'Booking ID - nullable for BONUS payment type';

