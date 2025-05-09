package com.sia.credigo.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sia.credigo.model.PaymentResponse
import com.sia.credigo.model.Wallet
import com.sia.credigo.model.WalletTopUpRequest
import com.sia.credigo.network.RetrofitClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal

/**
 * ViewModel for wallet operations that aligns with backend capabilities.
 * Handles authenticated user's wallet operations.
 */
class WalletViewModel : ViewModel() {
    private val TAG = "WalletViewModel"
    private val walletService = RetrofitClient.walletService

    private val _userWallet = MutableLiveData<Wallet?>()
    val userWallet: LiveData<Wallet?> = _userWallet

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _paymentIntentData = MutableLiveData<Map<String, String>?>()
    val paymentIntentData: LiveData<Map<String, String>?> = _paymentIntentData
    
    // Add the missing _paymongoResponse LiveData
    private val _paymongoResponse = MutableLiveData<PaymentResponse?>()
    val paymongoResponse: LiveData<PaymentResponse?> = _paymongoResponse

    /**
     * Fetches the authenticated user's wallet from the backend
     * Matches: WalletController.getCurrentUserWallet()
     */
    fun fetchMyWallet() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                Log.d(TAG, "Fetching authenticated user wallet")
                val response = withContext(Dispatchers.IO) {
                    walletService.getMyWallet()
                }
                if (response.isSuccessful) {
                    val wallet = response.body()
                    _userWallet.postValue(wallet)
                    Log.d(TAG, "Successfully fetched wallet: ${wallet?.balance}")
                } else {
                    val errorCode = response.code()
                    val errorMessage = when (errorCode) {
                        401, 403 -> "Authentication error (code $errorCode). Please log in again."
                        404 -> "Wallet not found. Please contact support."
                        else -> "Error: ${response.code()} - ${response.message()}"
                    }
                    _errorMessage.postValue(errorMessage)
                    Log.e(TAG, "Failed to fetch wallet: $errorCode - ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Network error: ${e.message}")
                Log.e(TAG, "Exception fetching wallet: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * COMPATIBILITY METHOD: For existing activities that use this method.
     * In new code, prefer fetchMyWallet() instead.
     * 
     * Note: This doesn't match backend API which doesn't allow fetching by userId directly,
     * but is included for backward compatibility with existing code.
     */
    fun getWalletByUserId(userId: Long) {
        Log.d(TAG, "getWalletByUserId($userId) called - redirecting to fetchMyWallet()")
        fetchMyWallet() // Just fetch the authenticated user's wallet
    }

    /**
     * COMPATIBILITY METHOD: For existing code that updates wallet balance.
     * 
     * WARNING: This is not a proper implementation. In a production app,
     * balance changes should happen through backend API calls like deposit or purchase.
     * 
     * @return Always returns false to indicate this isn't a real implementation.
     */
    suspend fun updateWalletBalance(walletId: Long, newBalance: Double): Boolean {
        Log.w(TAG, "updateWalletBalance($walletId, $newBalance) called - this method is deprecated")
        _errorMessage.postValue("Direct wallet balance updates are not supported. Use top-up functionality instead.")
        return false
    }

    /**
     * Creates a payment intent for wallet top-up
     * Matches: WalletController.createWalletTopUpIntent()
     * 
     * @param amount The amount to top up in BigDecimal
     */
    fun createTopUpPaymentIntent(amount: BigDecimal) {
        if (amount <= BigDecimal.ZERO) {
            _errorMessage.postValue("Amount must be greater than zero")
            return
        }

        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                Log.d(TAG, "Creating payment intent for amount: $amount")
                val request = WalletTopUpRequest(amount)
                val response = withContext(Dispatchers.IO) {
                    walletService.createWalletTopUpIntent(request)
                }
                
                if (response.isSuccessful) {
                    val paymentData = response.body()
                    _paymentIntentData.postValue(paymentData)
                    Log.d(TAG, "Payment intent created successfully: $paymentData")
                    
                    // Refresh wallet after successful payment intent creation
                    fetchMyWallet()
                } else {
                    val errorCode = response.code()
                    val errorMsg = when (errorCode) {
                        401, 403 -> "Authentication error (code $errorCode). Please log in again."
                        400 -> "Invalid request. Check amount and try again."
                        else -> "Error: ${response.code()} - ${response.message()}"
                    }
                    _errorMessage.postValue(errorMsg)
                    Log.e(TAG, "Failed to create payment intent: $errorMsg")
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                _errorMessage.postValue(errorMsg)
                Log.e(TAG, "Exception creating payment intent: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
    
    /**
     * Directly top up wallet balance on the backend
     * Uses createWalletTopUpIntent() since there's no direct topup endpoint yet
     * 
     * @param amount The amount to add to the wallet balance
     * @param description Optional description for the transaction
     */
    fun topUpWallet(amount: BigDecimal, description: String = "Manual wallet top-up") {
        if (amount <= BigDecimal.ZERO) {
            _errorMessage.postValue("Amount must be greater than zero")
            return
        }

        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                Log.d(TAG, "Topping up wallet with amount: $amount")
                val request = WalletTopUpRequest(amount, description)
                
                // Temporarily use payment intent endpoint until backend has direct topup
                val response = withContext(Dispatchers.IO) {
                    walletService.createWalletTopUpIntent(request)
                }
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Payment intent created for wallet top-up: ${response.body()}")
                    // Fetch updated wallet to reflect new balance
                    fetchMyWallet()
                } else {
                    val errorCode = response.code()
                    val errorMsg = when (errorCode) {
                        401, 403 -> "Authentication error (code $errorCode). Please log in again."
                        400 -> "Invalid request. Check amount and try again."
                        else -> "Error: ${response.code()} - ${response.message()}"
                    }
                    _errorMessage.postValue(errorMsg)
                    Log.e(TAG, "Failed to top up wallet: $errorMsg")
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                _errorMessage.postValue(errorMsg)
                Log.e(TAG, "Exception topping up wallet: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
    
    /**
     * Clears any previous error message
     */
    fun clearErrorMessage() {
        _errorMessage.postValue(null)
    }

    // Add a direct topup method as a fallback
    /**
     * Direct wallet topup method that bypasses PayMongo (for testing and fallback)
     * This uses the /api/wallet/topup endpoint directly
     */
    fun directTopupWallet(amount: BigDecimal) {
        if (amount <= BigDecimal.ZERO) {
            _errorMessage.postValue("Amount must be greater than zero")
            return
        }

        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                Log.d(TAG, "Performing direct wallet topup for amount: $amount")
                val request = WalletTopUpRequest(amount, "direct")
                
                val response = withContext(Dispatchers.IO) {
                    walletService.topupWallet(request)
                }
                
                if (response.isSuccessful && response.body() != null) {
                    val updatedWallet = response.body()
                    Log.d(TAG, "Direct topup successful: $updatedWallet")
                    _userWallet.postValue(updatedWallet)
                    
                    // Create a "fake" payment response to indicate success
                    _paymongoResponse.postValue(PaymentResponse(
                        clientSecret = "direct_topup_${System.currentTimeMillis()}",
                        checkoutUrl = null,
                        message = "Direct topup successful"
                    ))
                } else {
                    val errorCode = response.code()
                    var errorBody: String? = null
                    try {
                        errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Error response body: $errorBody")
                    } catch (e: Exception) {
                        Log.e(TAG, "Could not read error body: ${e.message}")
                    }
                    
                    val errorMsg = when (errorCode) {
                        401, 403 -> "Authentication error (code $errorCode). Please log in again."
                        400 -> "Invalid request. Check amount and try again. Details: $errorBody"
                        404 -> "Direct topup API endpoint not found (code 404)."
                        500, 502, 503, 504 -> "Server error (code $errorCode). Please try again later."
                        else -> "Error: $errorCode - ${response.message()}"
                    }
                    _errorMessage.postValue(errorMsg)
                    Log.e(TAG, "Failed to perform direct topup: $errorMsg")
                }
            } catch (e: Exception) {
                val errorMsg = "Network error during direct topup: ${e.message}"
                _errorMessage.postValue(errorMsg)
                Log.e(TAG, "Exception during direct topup: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Updates wallet balance LOCALLY ONLY (no API call)
     * This is for fallback when the server is unavailable
     * 
     * @param wallet The wallet with updated balance
     */
    fun updateWalletBalance(wallet: Wallet) {
        Log.d(TAG, "Updating wallet locally: $wallet")
        _userWallet.postValue(wallet)
    }
} 