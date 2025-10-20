-- Migration: Update package_interaction table
-- Change from boolean is_like to enum interaction_type

-- Step 1: Add new column with default value
ALTER TABLE package_interaction 
ADD COLUMN IF NOT EXISTS interaction_type VARCHAR(20);

-- Step 2: Migrate existing data (if any)
-- Convert is_like boolean to interaction_type enum
UPDATE package_interaction 
SET interaction_type = CASE 
    WHEN is_like = true THEN 'LIKE'
    WHEN is_like = false THEN 'DISLIKE'
    ELSE 'LIKE'
END
WHERE interaction_type IS NULL;

-- Step 3: Make interaction_type NOT NULL
ALTER TABLE package_interaction 
ALTER COLUMN interaction_type SET NOT NULL;

-- Step 4: Drop old column
ALTER TABLE package_interaction 
DROP COLUMN IF EXISTS is_like;

-- Step 5: Add unique constraint to prevent duplicate interactions
ALTER TABLE package_interaction 
DROP CONSTRAINT IF EXISTS unique_user_package_interaction;

ALTER TABLE package_interaction 
ADD CONSTRAINT unique_user_package_interaction UNIQUE (user_id, package_id);

