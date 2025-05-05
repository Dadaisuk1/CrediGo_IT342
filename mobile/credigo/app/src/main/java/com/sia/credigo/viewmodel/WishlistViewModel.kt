package com.sia.credigo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sia.credigo.model.Wishlist
import com.sia.credigo.network.RetrofitClient
import com.sia.credigo.network.models.BaseResponse
import kotlinx.coroutines.launch
import retrofit2.Response

class WishlistViewModel : ViewModel() {
    private val wishlistService = RetrofitClient.wishlistService

    private val _allWishlists = MutableLiveData<List<Wishlist>>()
    val allWishlists: LiveData<List<Wishlist>> = _allWishlists

    private val _userWishlist = MutableLiveData<List<Wishlist>>()
    val userWishlist: LiveData<List<Wishlist>> = _userWishlist

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadWishlists() {
        viewModelScope.launch {
            try {
                val response: Response<BaseResponse<List<Wishlist>>> = wishlistService.getAllWishlists()
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            _allWishlists.value = apiResponse.data ?: emptyList()
                        } else {
                            _errorMessage.value = apiResponse.message ?: "Failed to load wishlists"
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

    suspend fun getWishlistById(wishlistId: Long): Wishlist? {
        return try {
            val response: Response<BaseResponse<Wishlist>> = wishlistService.getWishlistById(wishlistId)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        apiResponse.data
                    } else {
                        _errorMessage.value = apiResponse.message ?: "Failed to get wishlist"
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

    fun getUserWishlist(userId: Long) {
        viewModelScope.launch {
            try {
                // For now, we'll filter all wishlists by userId
                val response: Response<BaseResponse<List<Wishlist>>> = wishlistService.getAllWishlists()
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            val userWishlists = apiResponse.data?.filter { it.userid == userId } ?: emptyList()
                            _userWishlist.value = userWishlists
                        } else {
                            _errorMessage.value = apiResponse.message ?: "Failed to get user wishlist"
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

    fun removeFromWishlist(userId: Long, productId: Long) {
        viewModelScope.launch {
            try {
                val response: Response<BaseResponse<Void>> = wishlistService.removeFromWishlist(productId)
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            // Update the user's wishlist after removal
                            getUserWishlist(userId)
                        } else {
                            _errorMessage.value = apiResponse.message ?: "Failed to remove from wishlist"
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

    fun addToWishlist(wishlist: Wishlist) {
        viewModelScope.launch {
            try {
                // Pass the correct parameter (e.g., wishlist.id)
                val response: Response<BaseResponse<Void>> = wishlistService.addToWishlist(wishlist.wishlistid)
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            // Update the user's wishlist after addition
                            getUserWishlist(wishlist.userid)
                        } else {
                            _errorMessage.value = apiResponse.message ?: "Failed to add to wishlist"
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
