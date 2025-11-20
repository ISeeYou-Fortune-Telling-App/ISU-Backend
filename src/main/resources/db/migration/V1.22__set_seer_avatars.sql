-- V1.22__set_seer_avatars.sql
-- Assign a random avatar_url to seer users (explicit list)
-- This migration is written for PostgreSQL (see application.yaml dialect)

-- The approach: for each matching user row, pick one URL from the ARRAY at random
-- and update that user's avatar_url. Quoted "user" name matches existing migrations.

UPDATE "user"
SET avatar_url = (
  ARRAY[
    'https://res.cloudinary.com/dzpv3mfjt/image/upload/v1762779748/certificates/nakoeyhi7fcurckplw0m.jpg',
    'https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/dummy_avatar_3_ycoboh.jpg',
    'https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/dummy_avatar_1_jjq9xj.jpg',
    'https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/dummy_avatar_2_vw0znd.jpg'
  ])[floor(random() * 4 + 1)::int]
WHERE user_id IN (
  '550e8400-e29b-41d4-a716-446655440072',
  '550e8400-e29b-41d4-a716-446655440073',
  '550e8400-e29b-41d4-a716-446655440074',
  '550e8400-e29b-41d4-a716-446655440075',
  '550e8400-e29b-41d4-a716-446655440076',
  '550e8400-e29b-41d4-a716-446655440077',
  '550e8400-e29b-41d4-a716-446655440078',
  '550e8400-e29b-41d4-a716-446655440079',
  '550e8400-e29b-41d4-a716-446655440080'
) AND role = '1';

-- End of migration
