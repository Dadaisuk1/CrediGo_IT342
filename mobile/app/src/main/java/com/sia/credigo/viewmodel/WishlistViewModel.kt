package com.sia.credigo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sia.credigo.model.WishlistItem
import com.sia.credigo.network.RetrofitClient
import com.sia.credigo.network.models.BaseResponse
import kotlinx.coroutines.launch
import retrofit2.Response

class WishlistViewModel : ViewModel() {
    private val TAG = "WishlistViewModel"
    private val wishlistService = RetrofitClient.wishlistService

    private val _wishlistItems = MutableLiveData<List<WishlistItem>>()
    val wishlistItems: LiveData<List<WishlistItem>> = _wishlistItems

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Current user ID for wishlist operations
    private var currentUserId: Int? = null

    fun setCurrentUser(userId: Int) {
        if (userId <= 0) {
            Log.e(TAG, "Invalid user ID: $userId")
            return
        }
        
        Log.d(TAG, "Setting current user ID to: $userId")
        currentUserId = userId
        // Load wishlist for the new user
        loadUserWishlist()
    }

    fun loadUserWishlist() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "Fetching user's wishlist")
                val response = wishlistService.getCurrentUserWishlist()
                if (response.isSuccessful) {
                    val items = response.body() ?: emptyList()
                    Log.d(TAG, "Fetched ${items.size} wishlist items")
                    _wishlistItems.value = items
                } else {
                    Log.e(TAG, "Error fetching wishlist: ${response.code()} ${response.message()}")
                    _errorMessage.value = "Failed to load wishlist: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error: ${e.message}", e)
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addToWishlist(productId: Int) {
        // Always double-check that we have a current user
        val userId = currentUserId ?: run {
            Log.e(TAG, "Cannot add to wishlist: No current user set")
            _errorMessage.value = "Cannot add to wishlist: No user logged in"
            return
        }

        Log.d(TAG, "Adding product $productId to wishlist for user $userId")
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = wishlistService.addToWishlist(productId)
                if (response.isSuccessful) {
                    // Add the new item to our local list
                    response.body()?.let { newItem ->
                        val currentList = _wishlistItems.value?.toMutableList() ?: mutableListOf()
                        currentList.add(newItem)
                        _wishlistItems.value = currentList
                        Log.d(TAG, "Successfully added product $productId to wishlist")
                    }
                } else {
                    Log.e(TAG, "Error adding to wishlist: ${response.code()} ${response.message()}")
                    _errorMessage.value = "Failed to add to wishlist: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error: ${e.message}", e)
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeFromWishlist(productId: Int) {
        // Always double-check that we have a current user
        val userId = currentUserId ?: run {
            Log.e(TAG, "Cannot remove from wishlist: No current user set")
            _errorMessage.value = "Cannot remove from wishlist: No user logged in"
            return
        }

        Log.d(TAG, "Removing product $productId from wishlist for user $userId")
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = wishlistService.removeFromWishlist(productId)
                if (response.isSuccessful) {
                    // Remove the item from our local list
                    val currentList = _wishlistItems.value?.toMutableList() ?: mutableListOf()
                    currentList.removeAll { it.productId == productId }
                    _wishlistItems.value = currentList
                    Log.d(TAG, "Successfully removed product $productId from wishlist")
                } else {
                    Log.e(TAG, "Error removing from wishlist: ${response.code()} ${response.message()}")
                    _errorMessage.value = "Failed to remove from wishlist: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error: ${e.message}", e)
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Helper method to check if a product is in the wishlist
    fun isProductInWishlist(productId: Int): Boolean {
        return _wishlistItems.value?.any { it.productId == productId } ?: false
    }

    // Helper method to get wishlist item details for a product
    fun getWishlistItemForProduct(productId: Int): WishlistItem? {
        return _wishlistItems.value?.find { it.productId == productId }
    }
}
