package com.sia.credigo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.model.Users
import com.sia.credigo.network.RetrofitClient
import com.sia.credigo.network.models.BaseResponse
import com.sia.credigo.network.models.RegisterRequest
import com.sia.credigo.network.models.UpdateUserRequest
import kotlinx.coroutines.launch
import retrofit2.Response

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as CredigoApp
    private val userService = app.apiServices.userService

    private val _allUsers = MutableLiveData<List<Users>>()
    val allUsers: LiveData<List<Users>> = _allUsers

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Logged in user info
    private val _loggedInUser = MutableLiveData<Users?>()
    val loggedInUser: LiveData<Users?> = _loggedInUser

    // Current user being viewed or edited
    private val _currentUser = MutableLiveData<Users?>()
    val currentUser: LiveData<Users?> = _currentUser

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> = _password

    fun fetchUsers() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response: Response<BaseResponse<List<Users>>> = userService.getAllUsers()
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            _allUsers.value = apiResponse.data ?: emptyList()
                        } else {
                            _errorMessage.value = apiResponse.message ?: "User fetch failed"
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

    suspend fun getUserById(userId: Long): Users? {
        return try {
            val response: Response<BaseResponse<Users>> = userService.getUserById(userId)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        apiResponse.data
                    } else {
                        _errorMessage.value = apiResponse.message ?: "Failed to get user"
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

    // Get user by username
    suspend fun getUserByUsername(username: String): Users? {
        return try {
            val response = userService.getUserByUsername(username)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        apiResponse.data
                    } else {
                        _errorMessage.value = apiResponse.message ?: "Failed to get user by username"
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

    // Update user
    suspend fun updateUser(userId: Long, username: String? = null, password: String? = null,
                         phonenumber: String? = null): Boolean {
        return try {
            val updateRequest = UpdateUserRequest(
                username = username,
                password = password,
                phonenumber = phonenumber
            )
            val response = userService.updateUser(userId, updateRequest)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        // Update the logged in user if available
                        apiResponse.data?.let { updatedUser ->
                            _loggedInUser.postValue(updatedUser)
                        }
                        true
                    } else {
                        _errorMessage.postValue(apiResponse.message ?: "Update failed")
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

    // Get user by email
    suspend fun getUserByEmail(email: String): Users? {
        return try {
            val response = userService.getUserByEmail(email)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        apiResponse.data
                    } else {
                        _errorMessage.value = apiResponse.message ?: "Failed to get user by email"
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

    // Register new user
    suspend fun registerUser(email: String, password: String, username:String, phonenumber: String): Users? {
        return try {
            val registerRequest = RegisterRequest(
                email = email,
                password = password,
                username = username,
                phonenumber = phonenumber
            )
            val response = userService.registerUser(registerRequest)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        apiResponse.data
                    } else {
                        _errorMessage.value = apiResponse.message ?: "Registration failed"
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

    // Load user by ID
    fun loadUser(userId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val user = getUserById(userId.toLong())
                _currentUser.value = user
            } catch (e: Exception) {
                _errorMessage.value = "Error loading user: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }


}
