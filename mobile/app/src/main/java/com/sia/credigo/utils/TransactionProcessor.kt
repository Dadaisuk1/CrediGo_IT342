package com.sia.credigo.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.LifecycleOwner
import com.sia.credigo.model.Mail
import com.sia.credigo.model.Product
import com.sia.credigo.model.Transaction
import com.sia.credigo.model.TransactionType
import com.sia.credigo.model.Wallet
import com.sia.credigo.model.PurchaseRequest
import com.sia.credigo.network.RetrofitClient
import com.sia.credigo.viewmodel.MailViewModel
import com.sia.credigo.viewmodel.PlatformViewModel
import com.sia.credigo.viewmodel.TransactionViewModel
import com.sia.credigo.manager.WalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Random
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.math.BigDecimal

/**
 * A utility class that handles product purchase transactions consistently across the app
 */
object TransactionProcessor {
    private const val TAG = "TransactionProcessor"

    /**
     * Process a product purchase
     *
     * @param lifecycleOwner The lifecycle owner (Activity/Fragment)
     * @param context The context for showing Toast messages
     * @param product The product being purchased
     * @param userId The user's ID
     * @param transactionViewModel View model for transactions
     * @param mailViewModel View model for mail
     * @param platformViewModel View model for platforms/categories
     * @param onSuccess Callback on successful purchase
     * @param onError Callback on purchase error
     */
    fun processPurchase(
        lifecycleOwner: LifecycleOwner,
        context: Context,
        product: Product,
        userId: Long,
        transactionViewModel: TransactionViewModel,
        mailViewModel: MailViewModel,
        platformViewModel: PlatformViewModel,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // First check if user has sufficient balance
        val currentWallet = WalletManager.getCurrentWallet()
        
        if (currentWallet == null) {
            onError("Wallet information not available")
            return
        }
        
        if (currentWallet.balance < product.price) {
            onError("Insufficient balance")
            return
        }
        
        // Process the purchase
        lifecycleOwner.lifecycleScope.launch {
            try {
                // 1. Call backend purchase endpoint using TransactionApi instead of WalletApi
                val result = withContext(Dispatchers.IO) {
                    try {
                        // Create PurchaseRequest object instead of JSON
                        val purchaseRequest = PurchaseRequest(
                            productId = product.id,
                            quantity = 1,
                            gameAccountId = userId.toString() // Use user ID as game account ID
                        )
                        
                        // Call the transactions/purchase endpoint
                        val response = RetrofitClient.transactionService.purchaseProduct(purchaseRequest)
                        
                        if (response.isSuccessful) {
                            // Log the successful response
                            val responseBody = response.body()
                            Log.d(TAG, "Purchase success! Response: $responseBody")
                            responseBody
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "Unknown error"
                            Log.e(TAG, "Purchase failed with error: $errorBody")
                            throw Exception(errorBody)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to process purchase: ${e.message}", e)
                        null
                    }
                }
                
                if (result == null) {
                    onError("Failed to process transaction")
                    return@launch
                }
                
                // 2. Get platform/category details for receipt
                val platform = withContext(Dispatchers.IO) {
                    platformViewModel.getPlatformById(product.platformId)
                }
                val platformName = platform?.name ?: "Unknown"
                
                // 3. Generate game code
                val gameCode = generateGameCode()
                
                // 4. Create receipt mail
                val receiptMail = Mail(
                    userid = userId,
                    subject = "Purchase of ${platformName}'s ${product.name}",
                    message = """Hi there,
Thanks for your recent purchase of ${product.name} from ${platformName}. We hope you enjoy your experience!

Here's the code for your game:
Code: ${gameCode}

Use this code to top up your account!

Best regards,
— The CrediGo Team""",
                    isRead = false
                )
                
                // 5. Save mail
                withContext(Dispatchers.IO) {
                    try {
                        mailViewModel.createMail(receiptMail)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to create receipt mail: ${e.message}", e)
                        // Continue anyway - mail is not critical
                    }
                }
                
                // 6. Refresh wallet to update balance
                WalletManager.refreshWallet()
                
                // 7. Call success callback
                onSuccess()
                
                // 8. Show success Toast
                Toast.makeText(
                    context,
                    "Purchase successful! Check your mail for the code.",
                    Toast.LENGTH_SHORT
                ).show()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during purchase: ${e.message}", e)
                onError("Error processing purchase: ${e.message ?: "Unknown error"}")
            }
        }
    }
    
    /**
     * Generate a random game code in the format XXX-XXX
     */
    private fun generateGameCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = Random()
        val firstPart = (1..3)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
        val secondPart = (1..3)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
        return "$firstPart-$secondPart"
    }
} 