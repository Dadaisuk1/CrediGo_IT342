-- Add OAuth2 columns to users table
ALTER TABLE users
ADD COLUMN provider VARCHAR(20) NULL,
ADD COLUMN provider_id VARCHAR(255) NULL,
ADD COLUMN image_url VARCHAR(255) NULL;

-- Make password_hash nullable to support OAuth2 users
ALTER TABLE users
ALTER COLUMN password_hash DROP NOT NULL;
