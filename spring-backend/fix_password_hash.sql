-- Make password_hash nullable to support OAuth2 users
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;
