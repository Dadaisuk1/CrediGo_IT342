package com.sia.credigo.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sia.credigo.model.Wallet
import com.sia.credigo.model.WalletTopUpRequest
import com.sia.credigo.network.RetrofitClient
import kotlinx.coroutines.launch
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

    /**
     * Fetches the authenticated user's wallet from the backend
     * Matches: WalletController.getCurrentUserWallet()
     */
    fun fetchMyWallet() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "Fetching authenticated user wallet")
                val response = walletService.getMyWallet()
                if (response.isSuccessful) {
                    val wallet = response.body()
                    _userWallet.value = wallet
                    Log.d(TAG, "Successfully fetched wallet: ${wallet?.balance}")
                } else {
                    val errorCode = response.code()
                    val errorMessage = when (errorCode) {
                        401, 403 -> "Authentication error (code $errorCode). Please log in again."
                        404 -> "Wallet not found. Please contact support."
                        else -> "Error: ${response.code()} - ${response.message()}"
                    }
                    _errorMessage.value = errorMessage
                    Log.e(TAG, "Failed to fetch wallet: $errorCode - ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
                Log.e(TAG, "Exception fetching wallet: ${e.message}", e)
            } finally {
                _isLoading.value = false
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
            _errorMessage.value = "Amount must be greater than zero"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "Creating payment intent for amount: $amount")
                val request = WalletTopUpRequest(amount)
                val response = walletService.createWalletTopUpIntent(request)
                
                if (response.isSuccessful) {
                    val paymentData = response.body()
                    _paymentIntentData.value = paymentData
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
                    _errorMessage.value = errorMsg
                    Log.e(TAG, "Failed to create payment intent: $errorMsg")
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, "Exception creating payment intent: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Clears any previous error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
} 