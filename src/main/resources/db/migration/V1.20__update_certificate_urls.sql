-- Migration: set all certificate URLs to the new default image
-- This will update every row in the `certificate` table to point to the standard default image.

UPDATE certificate
SET certificate_url = 'https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755679772/default_rxtl0p.jpg';

