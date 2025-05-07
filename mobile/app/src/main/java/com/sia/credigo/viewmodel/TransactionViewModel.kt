package com.sia.credigo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sia.credigo.model.Transaction
import com.sia.credigo.network.RetrofitClient
import com.sia.credigo.network.TransactionApi
import com.sia.credigo.network.models.BaseResponse
import kotlinx.coroutines.launch
import retrofit2.Response

class TransactionViewModel : ViewModel() {
    private val transactionService = RetrofitClient.transactionService

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _allTransactions = MutableLiveData<List<Transaction>>()
    val allTransactions: LiveData<List<Transaction>> = _allTransactions

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _transactionCreated = MutableLiveData<Transaction?>()
    val transactionCreated: LiveData<Transaction?> = _transactionCreated

    fun fetchTransactions() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response: Response<BaseResponse<List<Transaction>>> = transactionService.getTransactions()
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            _transactions.value = apiResponse.data ?: emptyList()
                        } else {
                            _errorMessage.value = apiResponse.message ?: "Transaction fetch failed"
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

    fun getUserTransactions(userId: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response: Response<BaseResponse<List<Transaction>>> = transactionService.getTransactions()
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            // Filter transactions for the specific user
                            val userTransactions = apiResponse.data?.filter { it.userid == userId } ?: emptyList()
                            _allTransactions.value = userTransactions
                        } else {
                            _errorMessage.value = apiResponse.message ?: "Failed to get user transactions"
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

    fun createTransaction(transaction: Transaction) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response: Response<BaseResponse<Transaction>> = transactionService.createTransaction(transaction)
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            _transactionCreated.value = apiResponse.data
                        } else {
                            _errorMessage.value = apiResponse.message ?: "Failed to create transaction"
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

    suspend fun getTransactionDetails(id: Long): Transaction? {
        return try {
            val response: Response<BaseResponse<Transaction>> = transactionService.getTransactionById(id)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        apiResponse.data
                    } else {
                        _errorMessage.value = apiResponse.message ?: "Failed to get transaction details"
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

    suspend fun getTransactionById(id: Long): Transaction? {
        return getTransactionDetails(id)
    }

    fun getTransactionRepository(): TransactionApi? {
        return transactionService
    }
}
