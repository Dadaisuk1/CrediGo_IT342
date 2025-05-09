package com.sia.credigo.model
import java.math.BigDecimal
import com.google.gson.annotations.SerializedName

data class Transaction(
    @SerializedName("transactionid")
    val transactionid: Long = 0,
    @SerializedName("userid")
    val userid: Long = 0,
    @SerializedName("type")
    val type: String = "",
    @SerializedName("amount")
    val amount: Double = 0.0,
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    @SerializedName("transaction_id")
    val transaction_id: Long = 0,  // Alias for transactionid to maintain compatibility with existing code
    @SerializedName("transactionStatus")
    val transactionStatus: TransactionStatus? = null,
    @SerializedName("transactionType")
    val transactionType: TransactionType? = null,
    @SerializedName("description")
    val description: String? = null // Added to match backend expectations
)

data class TransactionResponse(
    val transactionId: Int,
    val userId: Int,
    val username: String,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val purchasePrice: BigDecimal,
    val totalAmount: BigDecimal,
    val gameAccountId: String,
    val gameServerId: String?,
    val status: TransactionStatus,
    val transactionTimestamp: String,
    val statusMessage: String?
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
