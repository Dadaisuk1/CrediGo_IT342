-- Drop existing check constraint if it exists
ALTER TABLE IF EXISTS wallet_transactions DROP CONSTRAINT IF EXISTS wallet_transactions_transaction_type_check;

-- Add new check constraint with updated transaction types
ALTER TABLE IF EXISTS wallet_transactions
ADD CONSTRAINT wallet_transactions_transaction_type_check
CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'PURCHASE_DEDUCTION', 'REFUND_CREDIT', 'PENDING', 'PENDING_DEPOSIT'));
