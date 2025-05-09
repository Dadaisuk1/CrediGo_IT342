package com.sia.credigo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sia.credigo.model.User
import com.sia.credigo.network.RetrofitClient
import com.sia.credigo.network.models.RegisterRequest
import com.sia.credigo.network.models.UpdateUserRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.lang.Exception

class UserViewModel(application: Application) : AndroidViewModel(application) {
    
    // Services for API calls
    private val usersApi = RetrofitClient.userService
    private val authApi = RetrofitClient.authService
    
    // LiveData for the current user
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    /**
     * Get user by email with timeout and better error handling
     */
    suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        try {
            withTimeout(5000) { // Reduced timeout for faster UI response
                try {
                    val response = usersApi.getUserByEmail(email)
                    if (response.isSuccessful && response.body() != null) {
                        return@withTimeout mapResponseToUser(response.body()!!)
                    }
                    return@withTimeout null
                } catch (e: Exception) {
                    println("Error checking email: ${e.message}")
                    return@withTimeout null
                }
            }
        } catch (e: Exception) {
            println("Error checking email: ${e.message}")
            return@withContext null
        }
    }

    /**
     * Get user by username with timeout and better error handling
     */
    suspend fun getUserByUsername(username: String): User? = withContext(Dispatchers.IO) {
        try {
            withTimeout(5000) { // Reduced timeout for faster UI response
                try {
                    val response = usersApi.getUserByUsername(username)
                    if (response.isSuccessful && response.body() != null) {
                        return@withTimeout mapResponseToUser(response.body()!!)
                    }
                    return@withTimeout null
                } catch (e: Exception) {
                    println("Error checking username: ${e.message}")
                    return@withTimeout null
                }
            }
        } catch (e: Exception) {
            println("Error checking username: ${e.message}")
            return@withContext null
        }
    }

    /**
     * Register user method - KEEP ONLY THIS VERSION
     */
    suspend fun registerUser(
        email: String,
        password: String,
        username: String,
        phonenumber: String
    ): User? = withContext(Dispatchers.IO) {
        try {
            val registerRequest = RegisterRequest(
                email = email,
                password = password,
                username = username,
                phoneNumber = phonenumber
            )
            
            val response = authApi.register(registerRequest)
            if (response.isSuccessful && response.body() != null) {
                val userResponse = response.body()!!
                return@withContext mapResponseToUser(userResponse)
            }
            return@withContext null
        } catch (e: Exception) {
            println("Error registering user: ${e.message}")
            throw e
        }
    }
    
    /**
     * Update the user profile
     */
    suspend fun updateProfile(
        userId: Long,
        username: String? = null,
        password: String? = null,
        phonenumber: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Create the update request with only the fields that need to be updated
            val updateRequest = UpdateUserRequest(
                username = username,
                password = password,
                phonenumber = phonenumber
            )
            
            // Call the API to update the profile
            val response = usersApi.updateProfile(userId, updateRequest)
            
            return@withContext response.isSuccessful
        } catch (e: Exception) {
            println("Error updating profile: ${e.message}")
            throw e
        }
    }
    
    /**
     * Load user information by ID and update the LiveData
     */
    suspend fun loadUser(userId: Int) {
        try {
            val response = usersApi.getUserById(userId.toLong())
            if (response.isSuccessful && response.body() != null) {
                val baseResponse = response.body()!!
                val userData = baseResponse.data
                
                if (userData != null) {
                    val user = User(
                        id = userData.id,
                        username = userData.username,
                        email = userData.email,
                        phonenumber = userData.phonenumber,
                        dateOfBirth = userData.dateOfBirth,
                        createdAt = userData.createdAt,
                        roles = userData.roles ?: emptySet(),
                        active = userData.active ?: true,
                        balance = userData.balance
                    )
                    _currentUser.postValue(user)
                } else {
                    _currentUser.postValue(null)
                }
            } else {
                _currentUser.postValue(null)
            }
        } catch (e: Exception) {
            println("Error loading user: ${e.message}")
            _currentUser.postValue(null)
        }
    }
    
    /**
     * Load the currently authenticated user
     */
    suspend fun loadCurrentUser(): User? = withContext(Dispatchers.IO) {
        try {
            val response = usersApi.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                val userResponse = response.body()!!
                return@withContext mapResponseToUser(userResponse)
            }
            return@withContext null
        } catch (e: Exception) {
            println("Error loading current user: ${e.message}")
            throw e
        }
    }
    
    // Helper function to map UserResponse to User model
    private fun mapResponseToUser(response: com.sia.credigo.model.UserResponse): User {
        return User(
            id = response.id,
            username = response.username,
            email = response.email,
            phonenumber = response.phoneNumber,
            dateOfBirth = response.dateOfBirth,
            createdAt = response.createdAt,
            roles = response.roles,
            active = response.active ?: true,
            balance = response.balance
        )
    }
}
