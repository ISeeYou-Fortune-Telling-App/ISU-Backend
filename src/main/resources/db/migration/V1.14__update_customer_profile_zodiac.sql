-- =============================================
-- Fortune Telling Application - Database Schema
-- Version: 1.14
-- Description: Update customer_profile with calculated zodiac_sign, chinese_zodiac, and five_elements
-- =============================================

-- =============================================
-- UPDATE CUSTOMER PROFILES WITH ZODIAC DATA
-- =============================================

-- Function to calculate Western Zodiac Sign based on month and day
-- This function returns Vietnamese zodiac names
CREATE OR REPLACE FUNCTION calculate_zodiac_sign(month INT, day INT)
RETURNS VARCHAR(255) AS $$
BEGIN
    RETURN CASE
        WHEN (month = 1 AND day >= 20) OR (month = 2 AND day <= 18) THEN 'Bảo Bình'
        WHEN (month = 2 AND day >= 19) OR (month = 3 AND day <= 20) THEN 'Song Ngư'
        WHEN (month = 3 AND day >= 21) OR (month = 4 AND day <= 19) THEN 'Bạch Dương'
        WHEN (month = 4 AND day >= 20) OR (month = 5 AND day <= 20) THEN 'Kim Ngưu'
        WHEN (month = 5 AND day >= 21) OR (month = 6 AND day <= 20) THEN 'Song Tử'
        WHEN (month = 6 AND day >= 21) OR (month = 7 AND day <= 22) THEN 'Cự Giải'
        WHEN (month = 7 AND day >= 23) OR (month = 8 AND day <= 22) THEN 'Sư Tử'
        WHEN (month = 8 AND day >= 23) OR (month = 9 AND day <= 22) THEN 'Xử Nữ'
        WHEN (month = 9 AND day >= 23) OR (month = 10 AND day <= 22) THEN 'Thiên Bình'
        WHEN (month = 10 AND day >= 23) OR (month = 11 AND day <= 21) THEN 'Bọ Cạp'
        WHEN (month = 11 AND day >= 22) OR (month = 12 AND day <= 21) THEN 'Nhân Mã'
        WHEN (month = 12 AND day >= 22) OR (month = 1 AND day <= 19) THEN 'Ma Kết'
        ELSE NULL
    END;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Function to calculate Chinese Zodiac based on year
-- This function returns Vietnamese zodiac animal names
CREATE OR REPLACE FUNCTION calculate_chinese_zodiac(year INT)
RETURNS VARCHAR(255) AS $$
DECLARE
    zodiac_animals VARCHAR(255)[] := ARRAY['Tý', 'Sửu', 'Dần', 'Mão', 'Thìn', 'Tỵ', 'Ngọ', 'Mùi', 'Thân', 'Dậu', 'Tuất', 'Hợi'];
BEGIN
    RETURN zodiac_animals[((year - 4) % 12) + 1];
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Function to calculate Five Elements based on year
-- This function returns Vietnamese element names
CREATE OR REPLACE FUNCTION calculate_five_elements(year INT)
RETURNS VARCHAR(255) AS $$
DECLARE
    elements VARCHAR(255)[] := ARRAY['Kim', 'Mộc', 'Thủy', 'Hỏa', 'Thổ'];
BEGIN
    RETURN elements[((year - 4) % 10 / 2) + 1];
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Update customer_profile with calculated values
UPDATE customer_profile cp
SET
    zodiac_sign = calculate_zodiac_sign(
        EXTRACT(MONTH FROM u.birth_date)::INT,
        EXTRACT(DAY FROM u.birth_date)::INT
    ),
    chinese_zodiac = calculate_chinese_zodiac(
        EXTRACT(YEAR FROM u.birth_date)::INT
    ),
    five_elements = calculate_five_elements(
        EXTRACT(YEAR FROM u.birth_date)::INT
    ),
    updated_at = CURRENT_TIMESTAMP
FROM "user" u
WHERE cp.customer_id = u.user_id
  AND u.birth_date IS NOT NULL
  AND u.role = 4; -- Only update for customers (role = 4)

-- Drop the temporary functions (optional - keep them if you want to use them later)
-- Uncomment the following lines if you want to drop the functions after use
-- DROP FUNCTION IF EXISTS calculate_zodiac_sign(INT, INT);
-- DROP FUNCTION IF EXISTS calculate_chinese_zodiac(INT);
-- DROP FUNCTION IF EXISTS calculate_five_elements(INT);

