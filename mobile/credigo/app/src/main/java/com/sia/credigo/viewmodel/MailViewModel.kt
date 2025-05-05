package com.sia.credigo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sia.credigo.model.Mail
import com.sia.credigo.network.RetrofitClient
import com.sia.credigo.network.models.BaseResponse
import kotlinx.coroutines.launch
import retrofit2.Response

class MailViewModel : ViewModel() {
    private val mailService = RetrofitClient.mailService

    private val _mails = MutableLiveData<List<Mail>>()
    val mails: LiveData<List<Mail>> = _mails

    // Alias for mails to maintain compatibility with existing code
    private val _userMails = MutableLiveData<List<Mail>>()
    val userMails: LiveData<List<Mail>> = _userMails

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Flag to indicate if there are purchase-related mails
    private val _hasPurchaseMail = MutableLiveData<Boolean>(false)
    val hasPurchaseMail: LiveData<Boolean> = _hasPurchaseMail

    // Count of unread mails
    private val _unreadMailCount = MutableLiveData<Int>(0)
    val unreadMailCount: LiveData<Int> = _unreadMailCount

    fun fetchMails() {
        viewModelScope.launch {
            try {
                val response: Response<BaseResponse<List<Mail>>> = mailService.getAllMails()
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            _mails.value = apiResponse.data ?: emptyList()
                        } else {
                            _errorMessage.value = apiResponse.message ?: "Mail fetch failed"
                        }
                    } ?: run { _errorMessage.value = "Empty response" }
                } else {
                    _errorMessage.value = "HTTP ${response.code()}: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.localizedMessage}"
            }
        }
    }

    suspend fun getMailDetails(id: Long): Mail? {
        return try {
            val response: Response<BaseResponse<Mail>> = mailService.getMailById(id)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        apiResponse.data
                    } else {
                        _errorMessage.value = apiResponse.message ?: "Failed to get mail"
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

    // Alias for getMailDetails to maintain compatibility with existing code
    suspend fun getMailById(id: Long): Mail? {
        return getMailDetails(id)
    }

    fun getUserMails(userId: Int) {
        viewModelScope.launch {
            try {
                val response: Response<BaseResponse<List<Mail>>> = mailService.getUserMails(userId)
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            val mailsList = apiResponse.data ?: emptyList()
                            _userMails.value = mailsList

                            // Update unread count and purchase mail flag
                            updateUnreadMailCount()
                        } else {
                            _errorMessage.value = apiResponse.message ?: "Failed to load user mails"
                        }
                    } ?: run { _errorMessage.value = "Empty response" }
                } else {
                    _errorMessage.value = "HTTP ${response.code()}: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.localizedMessage}"
            }
        }
    }

    fun updateUnreadMailCount() {
        val currentMails = _userMails.value ?: _mails.value ?: emptyList()
        val unreadCount = currentMails.count { !it.isRead }
        _unreadMailCount.value = unreadCount

        // Check if there are any purchase-related mails
        _hasPurchaseMail.value = currentMails.any { 
            it.subject.contains("purchase", ignoreCase = true) || 
            it.message.contains("purchase", ignoreCase = true) 
        }
    }

    fun updateMail(mail: Mail) {
        viewModelScope.launch {
            try {
                val response: Response<BaseResponse<Mail>> = mailService.updateMail(mail.mailid, mail)
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            // Refresh the mail list to reflect changes
                            fetchMails()
                            // Update unread count
                            updateUnreadMailCount()
                        } else {
                            _errorMessage.value = apiResponse.message ?: "Failed to update mail"
                        }
                    } ?: run { _errorMessage.value = "Empty response" }
                } else {
                    _errorMessage.value = "HTTP ${response.code()}: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.localizedMessage}"
            }
        }
    }

    fun deleteMail(mail: Mail) {
        viewModelScope.launch {
            try {
                val response: Response<BaseResponse<Void>> = mailService.deleteMail(mail.mailid)
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            // Refresh the mail list to reflect changes
                            fetchMails()
                            // Update unread count
                            updateUnreadMailCount()
                        } else {
                            _errorMessage.value = apiResponse.message ?: "Failed to delete mail"
                        }
                    } ?: run { _errorMessage.value = "Empty response" }
                } else {
                    _errorMessage.value = "HTTP ${response.code()}: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.localizedMessage}"
            }
        }
    }

    // Overload for backward compatibility
    fun deleteMail(mailId: Long) {
        viewModelScope.launch {
            try {
                val response: Response<BaseResponse<Void>> = mailService.deleteMail(mailId)
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            // Refresh the mail list to reflect changes
                            fetchMails()
                            // Update unread count
                            updateUnreadMailCount()
                        } else {
                            _errorMessage.value = apiResponse.message ?: "Failed to delete mail"
                        }
                    } ?: run { _errorMessage.value = "Empty response" }
                } else {
                    _errorMessage.value = "HTTP ${response.code()}: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.localizedMessage}"
            }
        }
    }

    fun createMail(mail: Mail) {
        viewModelScope.launch {
            try {
                val response: Response<BaseResponse<Mail>> = mailService.createMail(mail)
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            // Refresh the mail list to reflect changes
                            fetchMails()
                            // Update unread count
                            updateUnreadMailCount()
                        } else {
                            _errorMessage.value = apiResponse.message ?: "Failed to create mail"
                        }
                    } ?: run { _errorMessage.value = "Empty response" }
                } else {
                    _errorMessage.value = "HTTP ${response.code()}: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.localizedMessage}"
            }
        }
    }
}
