package com.sia.credigo.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sia.credigo.model.Product
import com.sia.credigo.model.Wallet
import com.sia.credigo.viewmodel.WalletViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

/**
 * Centralized wallet management singleton that maintains wallet state
 * across different activities and provides a single source of truth.
 */
object WalletManager {
    private const val TAG = "WalletManager"
    
    // ViewModel instance for API operations
    private val walletViewModel = WalletViewModel()
    
    // Coroutine scope for background operations
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Wallet state LiveData
    private val _walletState = MutableLiveData<WalletState>()
    val walletState: LiveData<WalletState> = _walletState
    
    /**
     * Initialize the wallet manager for the current user
     * Should be called once when user logs in
     */
    fun initialize(userId: Int) {
        Log.d(TAG, "Initializing WalletManager for user $userId")
        
        // Set initial loading state
        _walletState.value = WalletState.Loading
        
        // Observe wallet changes from ViewModel
        walletViewModel.userWallet.observeForever { wallet ->
            wallet?.let {
                Log.d(TAG, "Wallet updated: $it")
                _walletState.value = WalletState.Loaded(it)
            }
        }
        
        // Observe error messages
        walletViewModel.errorMessage.observeForever { error ->
            if (error != null) {
                Log.e(TAG, "Wallet error: $error")
                _walletState.value = WalletState.Error(error)
            }
        }
        
        // Fetch the wallet data
        refreshWallet()
    }
    
    /**
     * Refresh wallet data from the backend
     */
    fun refreshWallet() {
        Log.d(TAG, "Refreshing wallet data")
        walletViewModel.fetchMyWallet()
    }
    
    /**
     * Create payment intent for wallet top-up
     * @param amount Amount to top up
     * @return Payment intent data for further processing with payment gateway
     */
    fun createTopUpIntent(amount: BigDecimal) {
        if (amount <= BigDecimal.ZERO) {
            _walletState.value = WalletState.Error("Amount must be greater than zero")
            return
        }
        
        Log.d(TAG, "Creating top-up intent for $amount")
        _walletState.value = WalletState.Loading
        
        // Update wallet balance via the backend payment intent API
        walletViewModel.createTopUpPaymentIntent(amount)
    }
    
    /**
     * Purchase a product (simulate backend deduction)
     * In production, this should be a proper API call to the backend
     * 
     * @param product Product to purchase
     * @return Result containing success or failure
     */
    suspend fun purchase(product: Product): Result<Boolean> = withContext(Dispatchers.IO) {
        val currentState = _walletState.value
        
        if (currentState !is WalletState.Loaded) {
            Log.e(TAG, "Cannot purchase: wallet not loaded")
            return@withContext Result.failure(Exception("Wallet not loaded"))
        }
        
        val wallet = currentState.wallet
        
        if (wallet.balance < product.price) {
            Log.e(TAG, "Insufficient balance: ${wallet.balance} < ${product.price}")
            return@withContext Result.failure(Exception("Insufficient balance"))
        }
        
        try {
            // In production, this should call a proper API endpoint
            // This is just simulating the call for compatibility with existing code
            Log.d(TAG, "Simulating purchase of ${product.name} for ${product.price}")
            
            // Refresh wallet to get updated balance after purchase
            // In production, the backend would handle the actual deduction
            refreshWallet()
            
            return@withContext Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Purchase failed: ${e.message}", e)
            _walletState.postValue(WalletState.Error("Purchase failed: ${e.message}"))
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Get current wallet (if available)
     * @return Current wallet or null if not loaded
     */
    fun getCurrentWallet(): Wallet? {
        return if (_walletState.value is WalletState.Loaded) {
            (_walletState.value as WalletState.Loaded).wallet
        } else {
            null
        }
    }
    
    /**
     * Check if user has sufficient balance for a product
     * @param product Product to check
     * @return True if sufficient balance, false otherwise
     */
    fun hasSufficientBalance(product: Product): Boolean {
        val wallet = getCurrentWallet() ?: return false
        return wallet.balance >= product.price
    }
    
    /**
     * Reset wallet manager (call when user logs out)
     */
    fun reset() {
        Log.d(TAG, "Resetting WalletManager")
        _walletState.value = WalletState.Initial
    }
}

/**
 * Represents the possible states of the wallet
 */
sealed class WalletState {
    object Initial : WalletState()
    object Loading : WalletState()
    data class Loaded(val wallet: Wallet) : WalletState()
    data class Error(val message: String) : WalletState()
} 