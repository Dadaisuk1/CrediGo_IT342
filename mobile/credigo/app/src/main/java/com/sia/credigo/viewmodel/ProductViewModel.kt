package com.sia.credigo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sia.credigo.model.Platform
import com.sia.credigo.model.Product
import com.sia.credigo.network.RetrofitClient
import com.sia.credigo.network.models.BaseResponse
import kotlinx.coroutines.launch
import retrofit2.Response

class ProductViewModel : ViewModel() {
    private val productService = RetrofitClient.productService

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun allProducts() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response: Response<BaseResponse<List<Product>>> = productService.getAllProducts()
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            _products.value = apiResponse.data ?: emptyList()
                        } else {
                            _errorMessage.value = apiResponse.message ?: "Product fetch failed"
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

    suspend fun getProductDetails(id: Long): Product? {
        return try {
            val response: Response<BaseResponse<Product>> = productService.getProductById(id)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        apiResponse.data
                    } else {
                        _errorMessage.value = apiResponse.message ?: "Failed to get product details"
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

    // Method to get a product by name
    suspend fun getProductByName(name: String): Product? {
        // Get all products and find the one with matching name
        val allProducts = _products.value ?: return null
        return allProducts.find { it.name.equals(name, ignoreCase = true) }
    }

    // Method to get a category by ID
    suspend fun getPlatformById(categoryId: Long): Platform? {
        // Use PlatformViewModel to get the platform (category)
        val platformViewModel = PlatformViewModel()
        return platformViewModel.getPlatformById(categoryId)
    }

    // Method to get products by platform ID
    suspend fun getProductsByCategory(categoryId: Long): List<Product> {
        // First ensure we have products loaded
        if (_products.value == null) {
            val response: Response<BaseResponse<List<Product>>> = productService.getAllProducts()
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        _products.value = apiResponse.data ?: emptyList()
                    }
                }
            }
        }

        // Filter products by category ID
        return _products.value?.filter { it.platformid == categoryId } ?: emptyList()
    }
}
