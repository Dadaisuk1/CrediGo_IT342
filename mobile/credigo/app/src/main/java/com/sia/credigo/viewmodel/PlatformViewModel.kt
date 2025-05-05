package com.sia.credigo.viewmodel

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
    private val platformService = RetrofitClient.platformService

    private val _allPlatforms = MutableLiveData<List<Platform>>()
    val allPlatforms: LiveData<List<Platform>> = _allPlatforms

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchPlatforms() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response: Response<BaseResponse<List<Platform>>> = platformService.getAllPlatforms()
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            _allPlatforms.value = apiResponse.data ?: emptyList()
                        } else {
                            _errorMessage.value = apiResponse.message ?: "Platform fetch failed"
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

    suspend fun getPlatformById(platformId: Long): Platform? {
        return try {
            val response: Response<BaseResponse<Platform>> = platformService.getPlatformById(platformId)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        apiResponse.data
                    } else {
                        _errorMessage.value = apiResponse.message ?: "Failed to get platform"
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
}
