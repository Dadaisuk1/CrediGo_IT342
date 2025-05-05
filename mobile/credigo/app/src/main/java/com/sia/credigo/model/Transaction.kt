package com.sia.credigo.model
import java.math.BigDecimal
data class Transaction(
    val transactionid: Long = 0,
    val userid: Long = 0,
    val type: String = "",
    val amount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val transaction_id: Long = 0,  // Alias for transactionid to maintain compatibility with existing code
    val transactionStatus: TransactionStatus? = null,
    val transactionType: TransactionType? = null
)
data class TransactionResponse(
    val transactionId: Int,
    val productId: Int,
    val productName: String,
    val amount: BigDecimal,
    val status: TransactionStatus,
    val transactionDate: Long,
    val walletTransactionId: Int? = null
)

enum class TransactionStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    COMPLETED,
    FAILED,
    REFUNDED
}

enum class TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    PURCHASE,
    REFUND
}
