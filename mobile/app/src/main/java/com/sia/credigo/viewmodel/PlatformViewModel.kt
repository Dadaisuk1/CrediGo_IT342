package com.sia.credigo.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sia.credigo.model.Platform
import com.sia.credigo.network.RetrofitClient
import com.sia.credigo.network.models.BaseResponse
import kotlinx.coroutines.launch
import retrofit2.Response

class PlatformViewModel : ViewModel() {
    private val TAG = "PlatformViewModel"
    private val platformService = RetrofitClient.platformService

    private val _allPlatforms = MutableLiveData<List<Platform>>()
    val allPlatforms: LiveData<List<Platform>> = _allPlatforms

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        Log.d(TAG, "ViewModel initialized")
    }

    fun fetchPlatforms() {
        Log.d(TAG, "fetchPlatforms: Starting API call")
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "Making API call to get all platforms")
                val response = platformService.getAllPlatforms()
                
                Log.d(TAG, "API response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
                
                if (response.isSuccessful) {
                    val platforms = response.body() ?: emptyList()
                    Log.d(TAG, "Fetched ${platforms.size} platforms")
                    platforms.forEach { platform ->
                        Log.d(TAG, "Platform: id=${platform.id}, name=${platform.name}, logoUrl=${platform.logoUrl}")
                    }
                    _allPlatforms.value = platforms
                    
                    // If we got an empty list, create some sample platforms for development/testing
                    if (platforms.isEmpty()) {
                        Log.w(TAG, "Empty platform list returned from API, creating fallback data")
                        createFallbackPlatforms()
                    }
                } else {
                    val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                    Log.e(TAG, errorMsg)
                    _errorMessage.value = errorMsg
                    
                    // If API call fails, create some sample platforms for development/testing
                    createFallbackPlatforms()
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.localizedMessage}"
                Log.e(TAG, errorMsg, e)
                _errorMessage.value = errorMsg
                
                // If exception occurs, create some sample platforms for development/testing
                createFallbackPlatforms()
            } finally {
                _isLoading.value = false
                Log.d(TAG, "Finished loading platforms")
            }
        }
    }

    private fun createFallbackPlatforms() {
        Log.d(TAG, "Creating fallback platform data for development/testing")
        val fallbackPlatforms = listOf(
            Platform(
                id = 1, 
                name = "Mobile Legends",
                logoUrl = "drawable://img_ml",
                description = "Mobile Legends: Bang Bang is a mobile multiplayer online battle arena game"
            ),
            Platform(
                id = 2, 
                name = "Valorant",
                logoUrl = "drawable://img_valo",
                description = "Valorant is a free-to-play first-person tactical hero shooter"
            ),
            Platform(
                id = 3, 
                name = "Genshin Impact",
                logoUrl = "drawable://img_genshin",
                description = "Genshin Impact is an action role-playing game"
            ),
            Platform(
                id = 4, 
                name = "PUBG Mobile",
                logoUrl = "drawable://img_pubg",
                description = "PUBG Mobile is a free-to-play battle royale video game"
            )
        )
        _allPlatforms.postValue(fallbackPlatforms)
    }

    suspend fun getPlatformById(platformId: Int): Platform? {
        return try {
            val response = platformService.getPlatformById(platformId)
            if (response.isSuccessful) {
                response.body()
            } else {
                _errorMessage.value = "Failed to get platform details: ${response.message()}"
                null
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error: ${e.message}"
            null
        }
    }
}
