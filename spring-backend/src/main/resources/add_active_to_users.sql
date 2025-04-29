-- Migration: Add 'active' column to 'users' table
ALTER TABLE users ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
