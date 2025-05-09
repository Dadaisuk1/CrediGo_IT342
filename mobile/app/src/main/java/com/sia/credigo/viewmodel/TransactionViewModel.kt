package com.sia.credigo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sia.credigo.model.Transaction
import com.sia.credigo.network.RetrofitClient
import com.sia.credigo.network.TransactionApi
import com.sia.credigo.network.models.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class TransactionViewModel : ViewModel() {
    private val TAG = "TransactionViewModel"
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
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    transactionService.getTransactions()
                }
                
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            _transactions.postValue(apiResponse.data ?: emptyList())
                        } else {
                            _errorMessage.postValue(apiResponse.message ?: "Transaction fetch failed")
                        }
                    } ?: run { _errorMessage.postValue("Empty response") }
                } else {
                    _errorMessage.postValue("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Network error: ${e.localizedMessage}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun getUserTransactions(userId: Long) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    transactionService.getTransactions()
                }
                
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            val userTransactions = apiResponse.data?.filter { it.userid == userId } ?: emptyList()
                            _allTransactions.postValue(userTransactions)
                        } else {
                            _errorMessage.postValue(apiResponse.message ?: "Failed to get user transactions")
                        }
                    } ?: run { _errorMessage.postValue("Empty response") }
                } else {
                    _errorMessage.postValue("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Network error: ${e.localizedMessage}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun createTransaction(transaction: Transaction) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    transactionService.createTransaction(transaction)
                }
                
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            _transactionCreated.postValue(apiResponse.data)
                        } else {
                            _errorMessage.postValue(apiResponse.message ?: "Failed to create transaction")
                        }
                    } ?: run { _errorMessage.postValue("Empty response") }
                } else {
                    _errorMessage.postValue("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Network error: ${e.localizedMessage}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    suspend fun getTransactionDetails(id: Long): Transaction? {
        return try {
            val response = withContext(Dispatchers.IO) {
                transactionService.getTransactionById(id)
            }
            
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        apiResponse.data
                    } else {
                        _errorMessage.postValue(apiResponse.message ?: "Failed to get transaction details")
                        null
                    }
                } ?: run {
                    _errorMessage.postValue("Empty response")
                    null
                }
            } else {
                _errorMessage.postValue("HTTP ${response.code()}: ${response.message()}")
                null
            }
        } catch (e: Exception) {
            _errorMessage.postValue("Error: ${e.localizedMessage}")
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
