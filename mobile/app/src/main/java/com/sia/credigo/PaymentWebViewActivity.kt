package com.sia.credigo

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sia.credigo.manager.WalletManager
import android.os.Handler
import android.os.Looper

/**
 * Activity that displays a WebView for processing PayMongo payments
 */
class PaymentWebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: ImageView
    private lateinit var titleTextView: TextView
    
    private var paymentUrl: String = ""
    private var returnUrl: String = "credigo://payment/success"
    private var loadStartTime: Long = 0
    private var maxLoadTime: Long = 30000 // 30 seconds timeout
    
    private val TAG = "PaymentWebViewActivity"
    private val timeoutHandler = Handler(Looper.getMainLooper())

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_webview)
        
        // Extract payment URL from intent
        paymentUrl = intent.getStringExtra("PAYMENT_URL") ?: ""
        returnUrl = intent.getStringExtra("RETURN_URL") ?: returnUrl
        
        if (paymentUrl.isEmpty()) {
            Log.e(TAG, "No payment URL provided")
            Toast.makeText(this, "Error: No payment URL provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        Log.d(TAG, "Payment URL: $paymentUrl")
        Log.d(TAG, "Return URL: $returnUrl")
        
        // Initialize views
        webView = findViewById(R.id.payment_web_view)
        progressBar = findViewById(R.id.progress_bar)
        backButton = findViewById(R.id.back_button)
        titleTextView = findViewById(R.id.title_text_view)
        
        // Set up back button
        backButton.setOnClickListener {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
        
        // Set up WebView
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(true)
            javaScriptCanOpenWindowsAutomatically = true
            cacheMode = WebSettings.LOAD_NO_CACHE // Avoid caching issues
        }
        
        webView.webViewClient = object : WebViewClient() {
            private var loadCount = 0
            private val MAX_LOAD_ATTEMPTS = 2
            private val urlHistory = mutableSetOf<String>()
            
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                Log.d(TAG, "Loading URL: $url")
                
                // Check if this is a return URL (success or cancel)
                if (url.startsWith(returnUrl) || url.contains("payment/success") || 
                    url.contains("paymongo.com/success") || url.contains("payment_success")) {
                    // Payment successful
                    Log.d(TAG, "Payment successful: $url")
                    handlePaymentSuccess()
                    return true
                } else if (url.contains("cancel") || url.contains("failed") || url.contains("error")) {
                    // Payment failed or cancelled
                    Log.d(TAG, "Payment failed or cancelled: $url")
                    handlePaymentCancelled()
                    return true
                }
                
                // Handle potential redirect loops
                if (urlHistory.contains(url)) {
                    loadCount++
                    if (loadCount > MAX_LOAD_ATTEMPTS) {
                        Log.e(TAG, "Detected redirect loop after $MAX_LOAD_ATTEMPTS attempts. Breaking loop.")
                        handlePaymentError("Payment gateway error: redirect loop detected")
                        return true
                    }
                }
                
                // Add URL to history
                urlHistory.add(url)
                if (urlHistory.size > 10) {
                    // Keep history size reasonable by removing oldest entries
                    urlHistory.toList().subList(0, urlHistory.size - 10).forEach { urlHistory.remove(it) }
                }
                
                // Reset timeout for new URL loads
                resetLoadTimeout()
                
                // Let the WebView handle the URL
                return false
            }
            
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                
                // Cancel timeout since page loaded
                timeoutHandler.removeCallbacksAndMessages(null)
                
                // Extract and set page title if available
                view.title?.let {
                    if (it.isNotEmpty()) {
                        titleTextView.text = it
                    }
                }
                
                // Inject JavaScript to detect various PayMongo errors
                injectErrorDetectionJavaScript(view)
            }
            
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                super.onReceivedError(view, request, error)
                progressBar.visibility = View.GONE
                
                val errorDescription = error.description?.toString() ?: "Unknown error"
                val errorCode = error.errorCode
                Log.e(TAG, "WebView error: $errorDescription (code: $errorCode) for URL: ${request.url}")
                
                // Only show error for main frame loads, not resources
                if (request.isForMainFrame) {
                    if (errorDescription.contains("ERR_CONNECTION_REFUSED") || 
                        errorDescription.contains("ERR_FAILED") ||
                        errorDescription.contains("ERR_CONNECTION_TIMED_OUT")) {
                        handlePaymentError("Connection to payment gateway failed: $errorDescription")
                    } else {
                        // Show error Toast but don't automatically finish for other errors
                        // unless they're critical
                        val isCritical = errorCode == ERROR_HOST_LOOKUP || 
                                         errorCode == ERROR_CONNECT || 
                                         errorCode == ERROR_TIMEOUT
                        
                        Toast.makeText(
                            this@PaymentWebViewActivity,
                            "Error: $errorDescription",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        if (isCritical) {
                            handlePaymentError("Critical payment error: $errorDescription")
                        }
                    }
                }
            }
            
            override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
                super.onReceivedHttpError(view, request, errorResponse)
                
                // Only handle main frame HTTP errors
                if (request.isForMainFrame) {
                    val statusCode = errorResponse.statusCode
                    Log.e(TAG, "HTTP error: $statusCode for URL: ${request.url}")
                    
                    if (statusCode >= 400) {
                        handlePaymentError("Payment server returned error: $statusCode")
                    }
                }
            }
        }
        
        // Set up progress tracking
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                if (newProgress < 100) {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                } else {
                    progressBar.visibility = View.GONE
                }
            }
            
            override fun onReceivedTitle(view: WebView, title: String) {
                super.onReceivedTitle(view, title)
                titleTextView.text = title
            }
            
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                val message = consoleMessage.message()
                val lineNumber = consoleMessage.lineNumber()
                val sourceId = consoleMessage.sourceId()
                val level = consoleMessage.messageLevel()
                
                Log.d(TAG, "Console: $message at $sourceId:$lineNumber")
                
                // Check for critical errors in console messages
                if (level == ConsoleMessage.MessageLevel.ERROR) {
                    if (message.contains("apiKey passed is invalid") || 
                        message.contains("PayMongo") || 
                        message.contains("payment") || 
                        message.contains("checkout")) {
                        
                        Log.e(TAG, "Critical error detected in console: $message")
                        // Don't immediately finish for every error, but detect specific ones
                        if (message.contains("apiKey passed is invalid")) {
                            handlePaymentError("PayMongo API key configuration error")
                        }
                    }
                }
                
                return super.onConsoleMessage(consoleMessage)
            }
        }
        
        // Start load timeout tracking
        resetLoadTimeout()
        
        // Load the payment URL
        webView.loadUrl(paymentUrl)
    }
    
    private fun injectErrorDetectionJavaScript(webView: WebView) {
        // First check for API key errors
        webView.evaluateJavascript(
            """
            (function() {
                var errorDetected = false;
                var errorType = "";
                
                // Check body text for common error messages
                if (document.body) {
                    var bodyText = document.body.innerText || "";
                    
                    if (bodyText.includes("apiKey passed is invalid")) {
                        errorDetected = true;
                        errorType = "api_key_invalid";
                    }
                    else if (bodyText.includes("Error") && bodyText.includes("payment")) {
                        errorDetected = true;
                        errorType = "payment_error";
                    }
                    else if (bodyText.includes("failed") && bodyText.includes("transaction")) {
                        errorDetected = true;
                        errorType = "transaction_failed";
                    }
                }
                
                // Check for error elements
                var errorElements = document.querySelectorAll(".error-message, .error, [data-error]");
                if (errorElements && errorElements.length > 0) {
                    errorDetected = true;
                    errorType = "error_element_found";
                }
                
                return JSON.stringify({
                    errorDetected: errorDetected,
                    errorType: errorType
                });
            })();
            """.trimIndent()
        ) { result ->
            try {
                // Remove quotes that wrap the JSON string
                val jsonStr = result.replace("^\"|\"$".toRegex(), "")
                
                // Use a more lenient approach for parsing the JSON
                val reader = android.util.JsonReader(java.io.StringReader(jsonStr))
                reader.isLenient = true  // Set the reader to be lenient
                
                reader.beginObject()
                var errorDetected = false
                var errorType = ""
                
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "errorDetected" -> errorDetected = reader.nextBoolean()
                        "errorType" -> errorType = reader.nextString()
                        else -> reader.skipValue()
                    }
                }
                
                reader.endObject()
                
                if (errorDetected) {
                    Log.e(TAG, "JavaScript detected error: $errorType")
                    when (errorType) {
                        "api_key_invalid" -> handlePaymentError("PayMongo API key configuration error")
                        "payment_error" -> handlePaymentError("Payment processing error")
                        "transaction_failed" -> handlePaymentError("Transaction failed")
                        "error_element_found" -> Log.w(TAG, "Error element found on page, but not treating as fatal")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing JavaScript result: ${e.message}")
                
                // Check if the result contains known error messages directly
                if (result.contains("apiKey") && result.contains("invalid")) {
                    Log.e(TAG, "Detected API key issue from raw response")
                    handlePaymentError("PayMongo API key configuration error")
                }
            }
        }
    }
    
    private fun resetLoadTimeout() {
        // Cancel any existing timeout
        timeoutHandler.removeCallbacksAndMessages(null)
        
        // Set new timeout
        loadStartTime = System.currentTimeMillis()
        timeoutHandler.postDelayed({
            val elapsed = System.currentTimeMillis() - loadStartTime
            if (elapsed >= maxLoadTime) {
                Log.e(TAG, "Payment page load timed out after ${elapsed}ms")
                handlePaymentError("Payment page load timed out")
            }
        }, maxLoadTime)
    }
    
    private fun handlePaymentSuccess() {
        Log.d(TAG, "Payment successful")
        Toast.makeText(this, "Payment successful!", Toast.LENGTH_SHORT).show()
        
        // Refresh wallet balance
        WalletManager.refreshWallet()
        
        // Set result and finish activity
        setResult(RESULT_OK)
        finish()
    }
    
    private fun handlePaymentCancelled() {
        Log.d(TAG, "Payment cancelled")
        Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show()
        
        // Set result and finish activity
        setResult(RESULT_CANCELED)
        finish()
    }
    
    private fun handlePaymentError(errorMessage: String) {
        Log.e(TAG, errorMessage)
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        
        // Set result and finish activity with RESULT_CANCELED to trigger fallback processing
        setResult(RESULT_CANCELED)
        finish()
    }
    
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            setResult(RESULT_CANCELED)
            super.onBackPressed()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up handler
        timeoutHandler.removeCallbacksAndMessages(null)
    }
} 