package com.sia.credigo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sia.credigo.model.Transaction
import com.sia.credigo.model.TransactionResponse
import com.sia.credigo.model.TransactionStatus
import com.sia.credigo.model.TransactionType
import com.sia.credigo.network.RetrofitClient
import com.sia.credigo.network.TransactionApi
import com.sia.credigo.network.models.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import android.util.Log

class TransactionViewModel : ViewModel() {
    private val TAG = "TransactionViewModel"
    private val transactionService = RetrofitClient.transactionService

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _allTransactions = MutableLiveData<List<Transaction>>()
    val allTransactions: LiveData<List<Transaction>> = _allTransactions
    
    private val _transactionHistory = MutableLiveData<List<TransactionResponse>>()
    val transactionHistory: LiveData<List<TransactionResponse>> = _transactionHistory

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _transactionCreated = MutableLiveData<Transaction?>()
    val transactionCreated: LiveData<Transaction?> = _transactionCreated

    /**
     * Fetch the authenticated user's transaction history from the new backend endpoint
     * and convert the response to work with our existing UI
     */
    fun fetchTransactionHistory() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    transactionService.getTransactionHistory()
                }
                
                if (response.isSuccessful) {
                    val transactionResponses = response.body()
                    if (transactionResponses != null) {
                        // Store the raw TransactionResponse objects
                        _transactionHistory.postValue(transactionResponses)
                        
                        // Convert TransactionResponse to Transaction for compatibility with existing UI
                        val convertedTransactions = transactionResponses.map { convertToTransaction(it) }
                        _allTransactions.postValue(convertedTransactions)
                        _transactions.postValue(convertedTransactions)
                        
                        Log.d(TAG, "Successfully fetched ${transactionResponses.size} transactions")
                    } else {
                        _errorMessage.postValue("Empty transaction history response")
                        Log.e(TAG, "Empty transaction history response")
                    }
                } else {
                    val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                    _errorMessage.postValue(errorMsg)
                    Log.e(TAG, "Failed to fetch transaction history: $errorMsg")
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.localizedMessage}"
                _errorMessage.postValue(errorMsg)
                Log.e(TAG, "Exception fetching transaction history", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Legacy method - use fetchTransactionHistory() instead
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

    // Legacy method - use fetchTransactionHistory() instead
    fun getUserTransactions(userId: Long) {
        fetchTransactionHistory()
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
        // First check if we have it in our local transactionHistory
        val transactionResponse = _transactionHistory.value?.find { it.transactionId.toLong() == id }
        if (transactionResponse != null) {
            return convertToTransaction(transactionResponse)
        }
        
        // Fall back to legacy method if not found
        return getTransactionDetails(id)
    }

    fun getTransactionRepository(): TransactionApi? {
        return transactionService
    }
    
    /**
     * Convert a TransactionResponse from the backend to the legacy Transaction model
     * to maintain compatibility with existing UI
     */
    private fun convertToTransaction(response: TransactionResponse): Transaction {
        // Parse timestamp if available
        val timestamp = try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            dateFormat.parse(response.transactionTimestamp)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
        
        // Create transaction type string from product name and status
        val transactionType = "${response.productName} - ${response.status}"
        
        return Transaction(
            transactionid = response.transactionId.toLong(),
            userid = response.userId.toLong(),
            type = transactionType,
            amount = response.totalAmount.toDouble(),
            timestamp = timestamp,
            transaction_id = response.transactionId.toLong(),
            transactionStatus = mapStatus(response.status),
            description = response.statusMessage
        )
    }
    
    /**
     * Map backend status to our local TransactionStatus enum
     */
    private fun mapStatus(status: TransactionStatus): TransactionStatus {
        // Assuming the enum values match between backend and mobile
        return status
    }
}
