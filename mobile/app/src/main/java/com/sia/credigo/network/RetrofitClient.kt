package com.sia.credigo.network

import android.util.Log
import com.sia.credigo.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton for managing Retrofit instances and API service creation
 */
object RetrofitClient {
    private const val TAG = "RetrofitClient"
    private const val BASE_URL = "https://credigo-it342.onrender.com/"
    private const val CONNECTION_TIMEOUT = 30L
    
    // Create regular Retrofit instance for non-authenticated calls
    val retrofit: Retrofit by lazy {
        Log.d(TAG, "Creating standard Retrofit instance")
        val client = createStandardClient()
        
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // Create authenticated services
    private var authenticatedRetrofit: Retrofit? = null
    
    // Auth API for login/register (uses non-authenticated client)
    val authService: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }
    
    // These services all use authenticated client when available
    val platformService: PlatformApi by lazy {
        getAuthenticatedRetrofitInstance().create(PlatformApi::class.java)
    }
    
    val productService: ProductApi by lazy {
        getAuthenticatedRetrofitInstance().create(ProductApi::class.java)
    }
    
    val wishlistService: WishlistApi by lazy {
        getAuthenticatedRetrofitInstance().create(WishlistApi::class.java)
    }
    
    val walletService: WalletApi by lazy {
        getAuthenticatedRetrofitInstance().create(WalletApi::class.java)
    }
    
    val userService: UsersApi by lazy {
        getAuthenticatedRetrofitInstance().create(UsersApi::class.java)
    }
    
    val transactionService: TransactionApi by lazy {
        getAuthenticatedRetrofitInstance().create(TransactionApi::class.java)
    }
    
    val mailService: MailApi by lazy {
        getAuthenticatedRetrofitInstance().create(MailApi::class.java)
    }
    
    val activityLogService: ActivityLogApi by lazy {
        getAuthenticatedRetrofitInstance().create(ActivityLogApi::class.java)
    }
    
    /**
     * Create OkHttpClient for standard (non-authenticated) requests
     */
    private fun createStandardClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Create OkHttpClient for authenticated requests
     */
    private fun createAuthenticatedClient(sessionManager: SessionManager): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Get authenticated Retrofit instance or fallback to standard
     * Internal method used by the lazy service properties
     */
    private fun getAuthenticatedRetrofitInstance(): Retrofit {
        return authenticatedRetrofit ?: retrofit
    }
    
    /**
     * Get authenticated Retrofit instance (for repository use)
     * This allows repositories to create their own service instances if needed
     */
    fun getAuthenticatedRetrofit(): Retrofit {
        return authenticatedRetrofit ?: retrofit
    }
    
    /**
     * Initialize the authenticated Retrofit instance with the user's session
     * Call this when the user logs in
     */
    fun initializeAuthenticatedRetrofit(sessionManager: SessionManager) {
        Log.d(TAG, "Initializing authenticated Retrofit")
        val client = createAuthenticatedClient(sessionManager)
        
        authenticatedRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        Log.d(TAG, "Authenticated Retrofit initialized successfully")
    }
    
    /**
     * Reset the authenticated Retrofit instance
     * Call this when the user logs out
     */
    fun resetAuthenticatedRetrofit() {
        Log.d(TAG, "Resetting authenticated Retrofit")
        authenticatedRetrofit = null
    }
}
