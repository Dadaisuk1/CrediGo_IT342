package com.sia.credigo.viewmodel

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
     * Fetches the authenticated user's wallet
     */
    fun fetchMyWallet() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = walletService.getMyWallet()
                if (response.isSuccessful) {
                    _userWallet.value = response.body()
                } else {
                    _errorMessage.value = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * COMPATIBILITY METHOD: For existing activities that use this method.
     * In new code, prefer fetchMyWallet() instead.
     */
    fun getWalletByUserId(userId: Long) {
        fetchMyWallet() // Just fetch the authenticated user's wallet
    }

    /**
     * COMPATIBILITY METHOD: For existing code that updates wallet balance.
     * In a real implementation, this should be done through proper API calls.
     */
    suspend fun updateWalletBalance(walletId: Long, newBalance: Double): Boolean {
        _errorMessage.postValue("Direct wallet balance updates are not supported. Use top-up functionality instead.")
        return false
    }

    /**
     * Creates a payment intent for wallet top-up
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
                val request = WalletTopUpRequest(amount)
                val response = walletService.createWalletTopUpIntent(request)
                
                if (response.isSuccessful) {
                    _paymentIntentData.value = response.body()
                } else {
                    _errorMessage.value = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
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