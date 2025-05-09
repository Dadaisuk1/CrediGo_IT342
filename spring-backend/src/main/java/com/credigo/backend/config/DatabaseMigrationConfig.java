package com.credigo.backend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseMigrationConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrateDatabase() {
        try {
            // Add OAuth2 columns if they don't exist
            jdbcTemplate.execute("DO $$ BEGIN " +
                    "BEGIN " +
                    "    ALTER TABLE users ADD COLUMN IF NOT EXISTS provider VARCHAR(20); " +
                    "EXCEPTION " +
                    "    WHEN duplicate_column THEN NULL; " +
                    "END; " +
                    "BEGIN " +
                    "    ALTER TABLE users ADD COLUMN IF NOT EXISTS provider_id VARCHAR(255); " +
                    "EXCEPTION " +
                    "    WHEN duplicate_column THEN NULL; " +
                    "END; " +
                    "BEGIN " +
                    "    ALTER TABLE users ADD COLUMN IF NOT EXISTS image_url VARCHAR(255); " +
                    "EXCEPTION " +
                    "    WHEN duplicate_column THEN NULL; " +
                    "END; " +
                    "END $$;");

            // Make password_hash nullable
            jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;");

            System.out.println("Database migration completed successfully.");
        } catch (Exception e) {
            System.err.println("Error during database migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
