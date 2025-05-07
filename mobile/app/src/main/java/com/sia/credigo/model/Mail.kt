package com.sia.credigo.model

data class Mail(
    val mailid: Long = 0,
    val userid: Long = 0,
    val transactionid: Long? = null,
    val transaction_id: Long? = null,  // Alias for transactionid to maintain compatibility with existing code
    val subject: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    var isRead: Boolean = false,
    val type: String = ""  // Added for compatibility with existing code
)
