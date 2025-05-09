package com.credigo.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.annotation.PostConstruct;

@Configuration
public class DatabaseInitializer {
    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initializeDatabase() {
        log.info("Updating database constraints for wallet_transactions...");
        try {
            // First, check if the constraint exists
            int count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_constraint WHERE conname = 'wallet_transactions_transaction_type_check'",
                Integer.class);

            if (count > 0) {
                // Drop the existing constraint
                jdbcTemplate.execute(
                    "ALTER TABLE wallet_transactions DROP CONSTRAINT IF EXISTS wallet_transactions_transaction_type_check");
                log.info("Dropped existing wallet_transactions_transaction_type_check constraint");
            }

            // Add the new constraint with updated values
            jdbcTemplate.execute(
                "ALTER TABLE wallet_transactions ADD CONSTRAINT wallet_transactions_transaction_type_check " +
                "CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'PURCHASE_DEDUCTION', 'REFUND_CREDIT', 'PENDING', 'PENDING_DEPOSIT'))");

            log.info("Successfully updated wallet_transactions constraint");
        } catch (Exception e) {
            // Log the error but don't prevent application startup
            log.error("Error updating wallet_transactions constraint: {}", e.getMessage(), e);
        }
    }
}
