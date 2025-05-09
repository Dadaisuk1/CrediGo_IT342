package com.sia.credigo.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.Response
import android.util.Log

object RetrofitClient {
    private const val TAG = "RetrofitClient"
    
    // Change from private to internal to allow access from other classes in the same package
    internal const val EMULATOR_URL = "https://credigo-it342.onrender.com/"
    internal const val LOCALHOST_URL = "http://192.168.1.5:8080/"
    internal const val PRODUCTION_URL = "https://credigo-it342.onrender.com/"

    // Use all three URLs in sequence for better connectivity
    var BASE_URL = EMULATOR_URL
    private const val DEBUG_MODE = true
    
    // Increased timeouts for better reliability
    private const val CONNECTION_TIMEOUT = 120L // seconds
    private const val READ_TIMEOUT = 120L // seconds
    private const val WRITE_TIMEOUT = 120L // seconds

    // Add headers to every request to avoid 403 errors
    private class DefaultHeadersInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val requestWithHeaders = originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "CredGo-Android-App")
                .build()
            
            Log.d(TAG, "Making request to: ${originalRequest.url}")
            return chain.proceed(requestWithHeaders)
        }
    }

    // Create the default client for non-authenticated requests (fixed duplication)
    private val defaultClient: OkHttpClient by lazy {
        Log.d(TAG, "Creating default OkHttpClient")
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (DEBUG_MODE) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(DefaultHeadersInterceptor())
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    // Try each URL in sequence until one works
    fun getWorkingRetrofit(): Retrofit {
        Log.d(TAG, "Attempting to find working API endpoint")
        val urls = listOf(EMULATOR_URL, PRODUCTION_URL, LOCALHOST_URL)
        
        for (url in urls) {
            try {
                Log.d(TAG, "Trying to connect to: $url")
                BASE_URL = url
                return Retrofit.Builder()
                    .baseUrl(url)
                    .client(defaultClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect to $url: ${e.message}")
            }
        }
        
        // If all failed, return default
        Log.w(TAG, "All connection attempts failed, using default URL: $BASE_URL")
        return retrofit
    }

    val retrofit: Retrofit by lazy {
        Log.d(TAG, "Creating Retrofit instance with base URL: $BASE_URL")
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(defaultClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Gets an alternate Retrofit instance using a different base URL
     */
    fun getAlternateRetrofit(): Retrofit {
        val alternateUrl = when (BASE_URL) {
            EMULATOR_URL -> PRODUCTION_URL
            PRODUCTION_URL -> LOCALHOST_URL
            else -> EMULATOR_URL
        }
        
        Log.d(TAG, "Using alternate URL: $alternateUrl")
        return Retrofit.Builder()
            .baseUrl(alternateUrl)
            .client(defaultClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Create a client with the AuthInterceptor
    fun createAuthenticatedClient(context: android.content.Context, lifecycleOwner: androidx.lifecycle.LifecycleOwner? = null): OkHttpClient {
        Log.d(TAG, "Creating authenticated OkHttpClient")
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (DEBUG_MODE) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(DefaultHeadersInterceptor())
            .addInterceptor(AuthInterceptor(context, lifecycleOwner))
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    // Public API services that don't require authentication
    val authService: AuthApi by lazy {
        Log.d(TAG, "Creating AuthApi service")
        retrofit.create(AuthApi::class.java)
    }

    // Create authenticated API services
    fun createAuthenticatedServices(context: android.content.Context, lifecycleOwner: androidx.lifecycle.LifecycleOwner? = null): AuthenticatedServices {
        Log.d(TAG, "Creating authenticated API services")
        val authenticatedClient = createAuthenticatedClient(context, lifecycleOwner)
        val authenticatedRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(authenticatedClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return AuthenticatedServices(
            mailService = authenticatedRetrofit.create(MailApi::class.java),
            productService = authenticatedRetrofit.create(ProductApi::class.java),
            wishlistService = authenticatedRetrofit.create(WishlistApi::class.java),
            platformService = authenticatedRetrofit.create(PlatformApi::class.java),
            userService = authenticatedRetrofit.create(UsersApi::class.java),
            walletService = authenticatedRetrofit.create(WalletApi::class.java),
            transactionService = authenticatedRetrofit.create(TransactionApi::class.java),
            authService = authenticatedRetrofit.create(AuthApi::class.java)
        )
    }

    // Class to hold authenticated API services
    data class AuthenticatedServices(
        val mailService: MailApi,
        val productService: ProductApi,
        val wishlistService: WishlistApi,
        val platformService: PlatformApi,
        val userService: UsersApi,
        val walletService: WalletApi,
        val transactionService: TransactionApi,
        val authService: AuthApi
    )

    // For backward compatibility - these will be deprecated
    val mailService: MailApi by lazy { 
        Log.d(TAG, "Creating MailApi service")
        retrofit.create(MailApi::class.java) 
    }
    val productService: ProductApi by lazy { 
        Log.d(TAG, "Creating ProductApi service")
        retrofit.create(ProductApi::class.java) 
    }
    val wishlistService: WishlistApi by lazy { 
        Log.d(TAG, "Creating WishlistApi service")
        retrofit.create(WishlistApi::class.java) 
    }
    val platformService: PlatformApi by lazy { 
        Log.d(TAG, "Creating PlatformApi service")
        retrofit.create(PlatformApi::class.java) 
    }
    val userService: UsersApi by lazy { 
        Log.d(TAG, "Creating UsersApi service")
        retrofit.create(UsersApi::class.java) 
    }
    val walletService: WalletApi by lazy { 
        Log.d(TAG, "Creating WalletApi service")
        retrofit.create(WalletApi::class.java) 
    }
    val transactionService: TransactionApi by lazy { 
        Log.d(TAG, "Creating TransactionApi service")
        retrofit.create(TransactionApi::class.java) 
    }
    val activityLogService: ActivityLogApi by lazy { 
        Log.d(TAG, "Creating ActivityLogApi service")
        retrofit.create(ActivityLogApi::class.java) 
    }
    
    init {
        Log.d(TAG, "RetrofitClient initialized with base URL: $BASE_URL")
    }
}
