package com.sia.credigo.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sia.credigo.model.Platform
import com.sia.credigo.model.Product
import com.sia.credigo.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Response
import java.math.BigDecimal

class ProductViewModel : ViewModel() {
    private val TAG = "ProductViewModel"
    private val productService = RetrofitClient.productService

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Fetch all products (overloaded method without platformId)
     */
    fun fetchProducts() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "Fetching all products")
                val response = productService.getAllProducts(null)
                if (response.isSuccessful) {
                    val products = response.body() ?: emptyList()
                    Log.d(TAG, "Fetched ${products.size} products")
                    _products.value = products
                } else {
                    Log.e(TAG, "Error fetching products: ${response.code()} ${response.message()}")
                    _errorMessage.value = "Failed to load products: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error: ${e.message}", e)
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Fetch products by platformId
     */
    fun fetchProducts(platformId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "Fetching products for platform ID: $platformId")
                val response = productService.getAllProducts(platformId)
                if (response.isSuccessful) {
                    val products = response.body() ?: emptyList()
                    Log.d(TAG, "Fetched ${products.size} products")
                    _products.value = products
                } else {
                    Log.e(TAG, "Error fetching products: ${response.code()} ${response.message()}")
                    _errorMessage.value = "Failed to load products: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error: ${e.message}", e)
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getProductDetails(id: Int): Product? {
        return try {
            val response = productService.getProductById(id)
            if (response.isSuccessful) {
                response.body()
            } else {
                _errorMessage.value = "Failed to get product details: ${response.message()}"
                null
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error: ${e.message}"
            null
        }
    }

    suspend fun getProductByName(name: String): Product? {
        return _products.value?.find { it.name.equals(name, ignoreCase = true) }
    }

    suspend fun getPlatformById(platformId: Int): Platform? {
        val platformViewModel = PlatformViewModel()
        return platformViewModel.getPlatformById(platformId.toLong()) // Convert Int to Long to fix type mismatch
    }

    // Method to get products by platform ID
    suspend fun getProductsByCategory(categoryId: Int): List<Product> {
        // First ensure we have products loaded
        if (_products.value == null) {
            try {
                val response = productService.getAllProducts(categoryId)
                if (response.isSuccessful) {
                    _products.value = response.body() ?: emptyList()
                } else {
                    Log.e(TAG, "Error fetching products: ${response.code()} ${response.message()}")
                    _errorMessage.value = "Failed to load products: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error: ${e.message}", e)
                _errorMessage.value = "Network error: ${e.message}"
            }
        }

        // Filter products by category ID
        return _products.value?.filter { it.platformid == categoryId } ?: emptyList()
    }

    // Helper method to format price for display
    fun formatPrice(price: BigDecimal): String {
        return String.format("₱%,.2f", price)
    }
}
