package com.sia.credigo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sia.credigo.adapters.ProductAdapter
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.manager.WalletManager
import com.sia.credigo.model.*

import com.sia.credigo.model.Mail
import com.sia.credigo.model.Wallet
import com.sia.credigo.utils.DialogUtils
import com.sia.credigo.utils.SortButtonsHandler
import com.sia.credigo.viewmodel.*
import java.math.BigDecimal
import kotlinx.coroutines.delay


class ProductsListActivity : AppCompatActivity() {
    companion object {
        private const val MAIL_LIST_REQUEST_CODE = 100
    }
    private var mailObserver: androidx.lifecycle.Observer<Int>? = null
    private lateinit var productViewModel: ProductViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var wishlistViewModel: WishlistViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var mailViewModel: MailViewModel
    private lateinit var walletViewModel: WalletViewModel
    private lateinit var sortButtonsHandler: SortButtonsHandler

    private var currentWallet: Wallet? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryTitleView: TextView
    private lateinit var confirmationPanel: CardView
    private lateinit var selectedProductView: TextView
    private lateinit var selectedPriceView: TextView
    private lateinit var buyButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var walletBalanceView: TextView
    private lateinit var errorMessageView: TextView
    private lateinit var walletIcon: ImageView
    private lateinit var mailIcon: ImageView
    private lateinit var categoryBackgroundView: ImageView

    private var currentUserId: Long = -1
    private var selectedProduct: Product? = null
    private val wishlistedProducts = mutableSetOf<Int>()
    private var categoryProducts = listOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products_list)

        // Initialize ViewModels
        productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        wishlistViewModel = ViewModelProvider(this).get(WishlistViewModel::class.java)
        transactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)
        mailViewModel = ViewModelProvider(this).get(MailViewModel::class.java)
        walletViewModel = ViewModelProvider(this).get(WalletViewModel::class.java)

        // Get user ID and category details from intent
        currentUserId = intent.getLongExtra("USER_ID", -1)
        val categoryId = intent.getLongExtra("CATEGORY_ID", -1)
        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Products"

        // Initialize views
        categoryTitleView = findViewById(R.id.tv_category_title)
        recyclerView = findViewById(R.id.rv_products)
        confirmationPanel = findViewById(R.id.confirmation_panel)
        selectedProductView = findViewById(R.id.tv_selected_product)
        selectedPriceView = findViewById(R.id.tv_selected_price)
        buyButton = findViewById(R.id.btn_buy)
        backButton = findViewById(R.id.btn_back)
        walletBalanceView = findViewById(R.id.tv_wallet_balance)
        errorMessageView = findViewById(R.id.tv_error_message)
        walletIcon = findViewById(R.id.wallet_icon)
        mailIcon = findViewById(R.id.mail_icon)
        categoryBackgroundView = findViewById(R.id.category_background)

        // Set category title
        categoryTitleView.text = categoryName

        // Set category background image
        setCategoryBackgroundImage(categoryName)

        // Initially hide confirmation panel and error message
        confirmationPanel.visibility = View.GONE
        errorMessageView.visibility = View.GONE

        // Load wallet data for current user
        if (currentUserId > 0) {
            walletViewModel.fetchMyWallet()
        }

        // Observe wallet data for live updates
        walletViewModel.userWallet.observe(this) { wallet ->
            wallet?.let {
                currentWallet = it
                // Update balance display with proper BigDecimal formatting
                val formattedBalance = String.format("%,.2f", it.balance)
                walletBalanceView.text = "₱$formattedBalance"
            }
        }

        // Set up click listeners
        walletIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        mailIcon.setOnClickListener {
            startActivity(Intent(this, MailsActivity::class.java))
        }

        // Set up mail observer
        mailObserver = androidx.lifecycle.Observer<Int> { count ->
            updateMailIcon(count, mailViewModel.hasPurchaseMail.value ?: false)
        }

        // Initial mail check
        mailViewModel.unreadMailCount.observe(this, mailObserver!!)

        // Observe purchase mail status
        mailViewModel.hasPurchaseMail.observe(this) { hasPurchaseMail ->
            updateMailIcon(mailViewModel.unreadMailCount.value ?: 0, hasPurchaseMail)
        }

        // Set up RecyclerView with 2 columns
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Set up the sort buttons
        setupSortButtons()

        // Load products for the selected category
        productViewModel.fetchProducts(categoryId.toInt())
        
        // Observe products
        productViewModel.products.observe(this) { products ->
            categoryProducts = products
            // Update sort buttons with the loaded products
            if (::sortButtonsHandler.isInitialized) {
                sortButtonsHandler.updateProducts(products)
            }
            updateAdapter(products)
        }

        // Observe loading state
        productViewModel.isLoading.observe(this) { isLoading ->
            // You can show/hide a loading indicator here if you have one
        }

        // Observe error messages
        productViewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        // Load wishlist items for current user
        wishlistViewModel.setCurrentUser(currentUserId.toInt())
        wishlistViewModel.loadUserWishlist()
        
        // Observe wishlist changes
        wishlistViewModel.wishlistItems.observe(this, Observer { items ->
            wishlistedProducts.clear()
            items.forEach { item ->
                wishlistedProducts.add(item.productId)
            }
            // Get products for the current category and update adapter
            CoroutineScope(Dispatchers.IO).launch {
                val products = productViewModel.getProductsByCategory(categoryId.toInt())
                withContext(Dispatchers.Main) {
                    updateAdapter(products)
                }
            }
        })

        // Set up buy button
        buyButton.setOnClickListener {
            selectedProduct?.let { product ->
                val wallet = currentWallet
                if (wallet != null && wallet.balance >= product.price) {
                    processPurchase(product, wallet)
                } else {
                    Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set up back button
        backButton.setOnClickListener {
            onBackPressed()
        }

        // Set up wallet icon click listener
        walletIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Set up mail icon click listener
        mailIcon.setOnClickListener {
            startActivityForResult(Intent(this, MailsActivity::class.java), MAIL_LIST_REQUEST_CODE)
        }
    }

    private fun updateAdapter(products: List<Product>) {
        recyclerView.adapter = ProductAdapter(
            products,
            onProductSelected = { product -> onProductSelected(product) },
            onWishlistClicked = { product -> toggleWishlist(product) },
            isInWishlist = { product -> wishlistedProducts.contains(product.id) }
        )
    }

    private fun onProductSelected(product: Product) {
        if (selectedProduct == product) {
            // Deselect: panel should disappear immediately
            selectedProduct = null
            hideConfirmationPanel()
            (recyclerView.adapter as? ProductAdapter)?.setSelectedProduct(null)
        } else {
            // Select: show panel and update info
            selectedProduct = product
            showConfirmationPanel(product)
            (recyclerView.adapter as? ProductAdapter)?.setSelectedProduct(product)
        }
        updateConfirmationPanelVisibility()
    }

    private fun updateBuyButtonState(userBalance: Double, productPrice: Double) {
        val canBuy = userBalance >= productPrice
        buyButton.isEnabled = canBuy
        errorMessageView.visibility = if (canBuy) View.GONE else View.VISIBLE
    }

    private fun generateRandomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = java.util.Random()
        val firstPart = (1..3)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
        val secondPart = (1..3)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
        return "$firstPart-$secondPart"
    }

    private fun toggleWishlist(product: Product) {
        if (wishlistedProducts.contains(product.id)) {
            // Show custom confirmation dialog for removing from wishlist
            DialogUtils.showCustomConfirmationDialog(
                context = this,
                title = "Remove from wishlist",
                message = "are you sure you want to remove this item?",
                onConfirm = {
                    // Remove from wishlist
                    wishlistViewModel.removeFromWishlist(product.id)
                    wishlistedProducts.remove(product.id)
                    // Update the adapter to reflect changes
                    (recyclerView.adapter as? ProductAdapter)?.updateWishlistState(product)
                    Toast.makeText(this, "Removed from wishlist", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            // Add to wishlist
            wishlistViewModel.addToWishlist(product.id)
            wishlistedProducts.add(product.id)
            Toast.makeText(this, "Added to wishlist", Toast.LENGTH_SHORT).show()
            // Update the adapter to reflect changes
            (recyclerView.adapter as? ProductAdapter)?.updateWishlistState(product)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAIL_LIST_REQUEST_CODE && resultCode == RESULT_OK) {
            // Force refresh mail count
            mailViewModel.updateUnreadMailCount()
        }
    }

    private fun updateMailIcon(unreadCount: Int, hasPurchaseMail: Boolean) {
        if (unreadCount > 0) {
            // If there are unread mails, use the unread icon
            mailIcon.setImageResource(R.drawable.ic_mail_unread)
        } else {
            // If there are no unread mails, use the regular icon
            mailIcon.setImageResource(R.drawable.ic_mail)
        }
    }

    override fun onResume() {
        super.onResume()
        // Remove and re-add observer to force refresh
        mailObserver?.let { observer ->
            mailViewModel.unreadMailCount.removeObserver(observer)
            mailViewModel.unreadMailCount.observe(this, observer)
        }

        // Force refresh purchase mail status
        mailViewModel.hasPurchaseMail.removeObservers(this)
        mailViewModel.hasPurchaseMail.observe(this) { hasPurchaseMail ->
            updateMailIcon(mailViewModel.unreadMailCount.value ?: 0, hasPurchaseMail)
        }
        
        // Refresh wallet data
        walletViewModel.fetchMyWallet()
    }

    private fun setupSortButtons() {
        val btnSortPopular = findViewById<Button>(R.id.btn_sort_popular)
        val btnSortHighLow = findViewById<Button>(R.id.btn_sort_high_low)
        val btnSortLowHigh = findViewById<Button>(R.id.btn_sort_low_high)
        val btnSortAZ = findViewById<Button>(R.id.btn_sort_a_z)

        if (btnSortPopular != null && btnSortHighLow != null && btnSortLowHigh != null && btnSortAZ != null) {
            // Create the sort buttons handler
            sortButtonsHandler = SortButtonsHandler(
                btnSortPopular,
                btnSortHighLow,
                btnSortLowHigh,
                btnSortAZ
            ) { products: List<Product> ->
                // Apply filtered products to the adapter
                updateAdapter(products)
            }

            // Setup visual styling for buttons
            btnSortPopular.setBackgroundResource(android.R.drawable.btn_default)
            btnSortHighLow.setBackgroundResource(android.R.drawable.btn_default)
            btnSortLowHigh.setBackgroundResource(android.R.drawable.btn_default)
            btnSortAZ.setBackgroundResource(android.R.drawable.btn_default)

            // Initialize with current products if available
            if (categoryProducts.isNotEmpty()) {
                sortButtonsHandler.updateProducts(categoryProducts)
            }
        }
    }

    /**
     * Set the background image for the category
     * The image resource follows the pattern: img_[category]_bg
     * Category name is converted to lowercase and spaces are replaced with underscores
     */
    private fun setCategoryBackgroundImage(categoryName: String) {
        // Format the category name to match resource naming conventions
        val formattedCategoryName = categoryName.lowercase().replace(" ", "_")

        // Map of common game names to their abbreviations as they appear in file names
        val abbreviationMap = mapOf(
            "mobile_legends" to "ml",
            "league_of_legends" to "lol",
            "valorant" to "valo",
            "call_of_duty" to "cod",
            "wild_rift" to "wildrift",
            "pubg_mobile" to "pubg",
            "genshin_impact" to "genshin",
            "zenless_zone_zero" to "zenless",
            "honkai_star_rail" to "star_rail"
        )

        // Check if we have an abbreviation for this category
        val categoryCode = abbreviationMap[formattedCategoryName] ?: formattedCategoryName

        // Create the drawable resource name - using the existing pattern observed in drawable folder
        val backgroundResourceName = "img_${categoryCode}_bg"

        // Get the resource ID
        val resourceId = resources.getIdentifier(
            backgroundResourceName,
            "drawable",
            packageName
        )

        // Set the background image if resource exists, otherwise use a default background
        if (resourceId != 0) {
            categoryBackgroundView.setImageResource(resourceId)
        } else {
            // Try with the original formatted name as a fallback
            if (categoryCode != formattedCategoryName) {
                val fallbackResourceName = "img_${formattedCategoryName}_bg"
                val fallbackResourceId = resources.getIdentifier(
                    fallbackResourceName,
                    "drawable",
                    packageName
                )

                if (fallbackResourceId != 0) {
                    categoryBackgroundView.setImageResource(fallbackResourceId)
                    return
                }
            }

            // Use a default background image if the specific one doesn't exist
            // We'll use img_notfound.png as our fallback since it already exists
            categoryBackgroundView.setImageResource(R.drawable.img_notfound)

            // Log a message about the missing resource
            android.util.Log.d("ProductsListActivity", "Background image not found: $backgroundResourceName")
        }
    }

    private fun showConfirmationPanel(product: Product) {
        confirmationPanel.visibility = View.VISIBLE
        findViewById<TextView>(R.id.tv_selected_product).text = product.name
        findViewById<TextView>(R.id.tv_selected_price).text = productViewModel.formatPrice(product.price)
        findViewById<TextView>(R.id.tv_error_message).visibility = View.GONE

        val wallet = currentWallet
        val buyBtn = findViewById<Button>(R.id.btn_buy)

        if (wallet != null && wallet.balance >= product.price) {
            buyBtn.isEnabled = true
            buyBtn.alpha = 1.0f
        } else {
            buyBtn.isEnabled = false
            buyBtn.alpha = 0.5f
            findViewById<TextView>(R.id.tv_error_message).visibility = View.VISIBLE
        }

        buyBtn.setOnClickListener {
            purchaseSelectedProduct()
        }
    }

    private fun hideConfirmationPanel() {
        confirmationPanel.visibility = View.GONE
    }

    private fun updateConfirmationPanelVisibility() {
        if (selectedProduct == null) {
            hideConfirmationPanel()
        } else {
            showConfirmationPanel(selectedProduct!!)
        }
    }

    private fun purchaseSelectedProduct() {
        selectedProduct?.let { product ->
            val wallet = currentWallet
            if (wallet != null && wallet.balance >= product.price) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Create transaction
                        val transaction = Transaction(
                            userid = currentUserId,
                            type = product.name,
                            amount = product.price.toDouble(),
                            timestamp = System.currentTimeMillis(),
                            transactionType = TransactionType.PURCHASE
                        )

                        // Insert transaction
                        val transactionId = withContext(Dispatchers.IO) {
                            transactionViewModel.createTransaction(transaction)
                        }

                        // Update wallet balance
                        val newBalance = wallet.balance.subtract(product.price)
                        // Note: This would normally use the proper API method in production
                        // This is a temporary fix for compatibility
                        withContext(Dispatchers.IO) {
                            walletViewModel.updateWalletBalance(wallet.id.toLong(), newBalance.toDouble())
                        }

                        // Get platform name
                        val platform = withContext(Dispatchers.IO) {
                            productViewModel.getPlatformById(product.platformId)
                        }
                        val platformName = platform?.name ?: "Unknown"

                        // Generate random code
                        val code = generateRandomCode()

                        // Create mail
                        val mail = Mail(
                            userid = currentUserId,
                            subject = "Purchase of ${platformName}'s ${product.name}",
                            message = """Hi there,
Thanks for your recent purchase of ${product.name} from ${platformName}. We hope you enjoy your experience!

Heres the code for your game:
Code: ${code}

Use this code to top up your account!

Best regards,
— The Credigo Team""",
                            timestamp = System.currentTimeMillis(),
                            isRead = false
                        )
                        withContext(Dispatchers.IO) {
                            mailViewModel.createMail(mail)
                        }

                        runOnUiThread {
                            confirmationPanel.visibility = View.GONE
                            Toast.makeText(this@ProductsListActivity, "Purchase successful!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@ProductsListActivity, "Error processing purchase", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processPurchase(product: Product, wallet: Wallet) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Toast.makeText(this@ProductsListActivity, "Processing purchase...", Toast.LENGTH_SHORT).show()
                
                // 1. Create purchase transaction
                val purchaseTransaction = Transaction(
                    userid = currentUserId,
                    type = product.name,
                    amount = product.price.toDouble(),
                    timestamp = System.currentTimeMillis(),
                    transactionType = TransactionType.PURCHASE
                )
                
                // 2. Create the transaction in the backend (using ViewModel that handles IO threading)
                transactionViewModel.createTransaction(purchaseTransaction)
                
                // 3. Get platform/category name for the receipt
                val platform = withContext(Dispatchers.IO) {
                    productViewModel.getPlatformById(product.platformId)
                }
                val platformName = platform?.name ?: "Unknown"
                
                // 4. Generate random code for the game
                val code = generateRandomCode()
                
                // 5. Create mail with purchase receipt
                val mail = Mail(
                    userid = currentUserId,
                    subject = "Purchase of ${platformName}'s ${product.name}",
                    message = """Hi there,
Thanks for your recent purchase of ${product.name} from ${platformName}. We hope you enjoy your experience!

Here's the code for your game:
Code: ${code}

Use this code to top up your account!

Best regards,
— The CrediGo Team""",
                    isRead = false
                )
                
                // 6. Save the mail (using IO)
                withContext(Dispatchers.IO) {
                    mailViewModel.createMail(mail)
                }
                
                // 7. Wait briefly for transaction to process
                withContext(Dispatchers.IO) {
                    delay(1000)
                }
                
                // 8. Refresh wallet to get updated balance (on main thread)
                WalletManager.refreshWallet()
                
                // 9. UI updates (already on main thread)
                confirmationPanel.visibility = View.GONE
                
                // Show success message
                Toast.makeText(
                    this@ProductsListActivity, 
                    "Purchase successful! Check your mail for the code.", 
                    Toast.LENGTH_SHORT
                ).show()
                
            } catch (e: Exception) {
                Log.e("ProductsListActivity", "Purchase error: ${e.message}", e)
                Toast.makeText(
                    this@ProductsListActivity,
                    "Error processing purchase: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
