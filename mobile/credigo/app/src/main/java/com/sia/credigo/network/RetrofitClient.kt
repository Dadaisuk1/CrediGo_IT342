package com.sia.credigo.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Multiple base URLs for different environments
    private const val EMULATOR_URL = "http://10.0.2.2:8080/api/" // For Android emulator
    private const val PRODUCTION_URL = "https://your-production-server.com/api/" // For production

    // Determine which URL to use
    private val BASE_URL: String = when {
        isEmulator() -> EMULATOR_URL
        else -> PRODUCTION_URL
    }

    private const val DEBUG_MODE = true // Set to false for production

    // Check if running on an emulator
    private fun isEmulator(): Boolean {
        return android.os.Build.PRODUCT.contains("sdk") || 
               android.os.Build.MODEL.contains("Emulator") ||
               android.os.Build.MODEL.contains("Android SDK")
    }

    // Check if in debug mode
    private fun isDebugMode(): Boolean {
        return DEBUG_MODE
    }

    // Create a client with the AuthInterceptor
    fun createAuthenticatedClient(context: android.content.Context, lifecycleOwner: androidx.lifecycle.LifecycleOwner? = null): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (DEBUG_MODE) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(context, lifecycleOwner))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Default client for non-authenticated requests
    private val defaultClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (DEBUG_MODE) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(defaultClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Public API services that don't require authentication
    val authService: AuthApi = retrofit.create(AuthApi::class.java)

    // Create authenticated API services
    fun createAuthenticatedServices(context: android.content.Context, lifecycleOwner: androidx.lifecycle.LifecycleOwner? = null): AuthenticatedServices {
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
            transactionService = authenticatedRetrofit.create(TransactionApi::class.java)
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
        val transactionService: TransactionApi
    )

    // For backward compatibility - these will be deprecated
    val mailService: MailApi by lazy { retrofit.create(MailApi::class.java) }
    val productService: ProductApi by lazy { retrofit.create(ProductApi::class.java) }
    val wishlistService: WishlistApi by lazy { retrofit.create(WishlistApi::class.java) }
    val platformService: PlatformApi by lazy { retrofit.create(PlatformApi::class.java) }
    val userService: UsersApi by lazy { retrofit.create(UsersApi::class.java) }
    val walletService: WalletApi by lazy { retrofit.create(WalletApi::class.java) }
    val transactionService: TransactionApi by lazy { retrofit.create(TransactionApi::class.java) }
    val activityLogService: ActivityLogApi by lazy { retrofit.create(ActivityLogApi::class.java) }
}
