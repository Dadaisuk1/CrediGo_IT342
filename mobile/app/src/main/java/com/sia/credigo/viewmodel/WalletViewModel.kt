package com.sia.credigo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sia.credigo.model.Wallet
import com.sia.credigo.network.RetrofitClient
import com.sia.credigo.network.models.BaseResponse
import com.sia.credigo.network.models.UpdateWalletRequest
import kotlinx.coroutines.launch
import retrofit2.Response

class WalletViewModel : ViewModel() {
    private val walletService = RetrofitClient.walletService

    private val _allWallets = MutableLiveData<List<Wallet>>()
    val allWallets: LiveData<List<Wallet>> = _allWallets

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _userWallet = MutableLiveData<Wallet?>()
    val userWallet: LiveData<Wallet?> = _userWallet

    fun fetchWallets() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response: Response<BaseResponse<List<Wallet>>> = walletService.getAllWallets()
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            _allWallets.value = apiResponse.data ?: emptyList()
                        } else {
                            _errorMessage.value = apiResponse.message ?: "Wallet fetch failed"
                        }
                    } ?: run { _errorMessage.value = "Empty response" }
                } else {
                    _errorMessage.value = "HTTP ${response.code()}: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getWalletById(walletId: Long): Wallet? {
        return try {
            val response: Response<BaseResponse<Wallet>> = walletService.getWalletById(walletId)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        apiResponse.data
                    } else {
                        _errorMessage.value = apiResponse.message ?: "Failed to get wallet"
                        null
                    }
                } ?: run {
                    _errorMessage.value = "Empty response"
                    null
                }
            } else {
                _errorMessage.value = "HTTP ${response.code()}: ${response.message()}"
                null
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error: ${e.localizedMessage}"
            null
        }
    }

    fun getWalletByUserId(userId: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response: Response<BaseResponse<Wallet>> = walletService.getWalletByUserId(userId)
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            _userWallet.value = apiResponse.data
                        } else {
                            _errorMessage.value = apiResponse.message ?: "Failed to get user wallet"
                        }
                    } ?: run { _errorMessage.value = "Empty response" }
                } else {
                    _errorMessage.value = "HTTP ${response.code()}: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun updateWalletBalance(walletId: Long, newBalance: Double): Boolean {
        return try {
            val updateRequest = UpdateWalletRequest(
                balance = newBalance
            )
            val response = walletService.updateWallet(walletId, updateRequest)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        // Update the user wallet if available
                        apiResponse.data?.let { updatedWallet ->
                            _userWallet.postValue(updatedWallet)
                        }
                        true
                    } else {
                        _errorMessage.postValue(apiResponse.message ?: "Wallet update failed")
                        false
                    }
                } ?: run {
                    _errorMessage.postValue("Empty response")
                    false
                }
            } else {
                _errorMessage.postValue("HTTP ${response.code()}: ${response.message()}")
                false
            }
        } catch (e: Exception) {
            _errorMessage.postValue("Error: ${e.localizedMessage}")
            false
        }
    }
}
