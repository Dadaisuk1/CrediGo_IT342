package com.sia.credigo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sia.credigo.adapters.ProductAdapter
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.model.Mail
import com.sia.credigo.model.Product
import com.sia.credigo.model.Transaction
import com.sia.credigo.model.TransactionType
import com.sia.credigo.model.Wallet
import com.sia.credigo.model.WishlistItem
import com.sia.credigo.utils.DialogUtils
import com.sia.credigo.viewmodel.*
import com.sia.credigo.viewmodel.PlatformViewModel
import com.sia.credigo.manager.WalletManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import kotlinx.coroutines.delay
import com.sia.credigo.utils.TransactionProcessor

class WishlistActivity : AppCompatActivity() {
    companion object {
        private const val MAIL_LIST_REQUEST_CODE = 100
    }
    private var mailObserver: androidx.lifecycle.Observer<Int>? = null
    private lateinit var wishlistViewModel: WishlistViewModel
    private lateinit var productViewModel: ProductViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var mailViewModel: MailViewModel
    private lateinit var walletViewModel: WalletViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var walletBalanceView: TextView
    private lateinit var walletIcon: ImageView
    private lateinit var mailIcon: ImageView
    private lateinit var confirmationPanel: CardView
    private lateinit var selectedProductView: TextView
    private lateinit var selectedPriceView: TextView
    private lateinit var buyButton: Button
    private lateinit var errorMessageView: TextView

    private var currentUserId: Long = -1
    private val wishlistedProducts = mutableSetOf<Int>()
    private val likedProducts = mutableListOf<Product>()
    private var selectedProduct: Product? = null
    private var currentWallet: Wallet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_likes)

        // Get user ID from CredigoApp
        val app = application as CredigoApp
        currentUserId = (app.loggedInuser?.id ?: -1).toLong()

        // Initialize ViewModels
        wishlistViewModel = ViewModelProvider(this).get(WishlistViewModel::class.java)
        productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        transactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)
        walletViewModel = ViewModelProvider(this).get(WalletViewModel::class.java)

        // Initialize views
        recyclerView = findViewById(R.id.rv_liked_products)
        walletBalanceView = findViewById(R.id.tv_balance)
        walletIcon = findViewById(R.id.iv_wallet)
        mailIcon = findViewById(R.id.mail_icon)
        confirmationPanel = findViewById(R.id.confirmation_panel)
        selectedProductView = findViewById(R.id.tv_selected_product)
        selectedPriceView = findViewById(R.id.tv_selected_price)
        buyButton = findViewById(R.id.btn_buy)
        errorMessageView = findViewById(R.id.tv_error_message)

        // Set up RecyclerView
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 columns

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
            startActivity(Intent(this, WalletActivity::class.java))
        }

        mailIcon.setOnClickListener {
            startActivityForResult(Intent(this, MailsActivity::class.java), MAIL_LIST_REQUEST_CODE)
        }

        // Initialize mail view model
        mailViewModel = ViewModelProvider(this).get(MailViewModel::class.java)

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

        // Load all products
        productViewModel.fetchProducts()
        productViewModel.products.observe(this, Observer { products ->
            updateLikedProducts(products)
        })

        // Load user's wishlist
        wishlistViewModel.setCurrentUser(currentUserId.toInt())
        wishlistViewModel.loadUserWishlist()

        // Observe wishlist changes
        wishlistViewModel.wishlistItems.observe(this, Observer { items ->
            // Clear previous wishlist
            wishlistedProducts.clear()

            // Add all product IDs from wishlist
            items.forEach { item ->
                wishlistedProducts.add(item.productId)
            }

            // Update liked products
            val currentProducts = productViewModel.products.value ?: emptyList()
            updateLikedProducts(currentProducts)
        })
    }

    private fun updateLikedProducts(allProducts: List<Product>) {
        // Filter products that are in the wishlist
        likedProducts.clear()
        likedProducts.addAll(allProducts.filter { product ->
            wishlistedProducts.contains(product.id)
        })

        // Update adapter
        recyclerView.adapter = ProductAdapter(
            likedProducts,
            onProductSelected = { product -> onProductSelected(product) },
            onWishlistClicked = { product -> toggleWishlist(product) },
            isInWishlist = { product -> wishlistedProducts.contains(product.id) }
        )
    }

    private fun onProductSelected(product: Product) {
        if (selectedProduct == product) {
            hideConfirmationPanel()
            selectedProduct = null
            (recyclerView.adapter as? ProductAdapter)?.apply {
                setSelectedProduct(null)
                notifyDataSetChanged() // Force immediate UI update
            }
        } else {
            showConfirmationPanel(product)
            selectedProduct = product
            (recyclerView.adapter as? ProductAdapter)?.setSelectedProduct(product)
        }
    }

    private fun showConfirmationPanel(product: Product) {
        confirmationPanel.visibility = View.VISIBLE
        selectedProductView.text = "${product.name} • E-Wallet"
        selectedPriceView.text = "₱${product.price}"
        errorMessageView.visibility = View.GONE
        val wallet = currentWallet
        if (wallet != null && wallet.balance >= product.price) {
            buyButton.isEnabled = true
            buyButton.alpha = 1.0f
        } else {
            buyButton.isEnabled = false
            buyButton.alpha = 0.5f
            errorMessageView.visibility = View.VISIBLE
        }
        buyButton.setOnClickListener {
            purchaseSelectedProduct()
        }
    }

    private fun hideConfirmationPanel() {
        confirmationPanel.visibility = View.GONE
    }

    private fun purchaseSelectedProduct() {
        selectedProduct?.let { product ->
            val wallet = currentWallet
            if (wallet != null && wallet.balance >= product.price) {
                processPurchase(product, wallet)
            } else {
                Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processPurchase(product: Product, wallet: Wallet) {
        // Show loading state
        val progressBar = findViewById<View>(R.id.progress_bar) ?: View(this).apply { 
            visibility = View.GONE 
        }
        progressBar.visibility = View.VISIBLE
        
        // Use the TransactionProcessor
        TransactionProcessor.processPurchase(
            lifecycleOwner = this,
            context = this,
            product = product,
            userId = currentUserId,
            transactionViewModel = transactionViewModel,
            mailViewModel = mailViewModel,
            platformViewModel = ViewModelProvider(this).get(PlatformViewModel::class.java),
            onSuccess = {
                // UI updates on success
                hideConfirmationPanel()
                progressBar.visibility = View.GONE
                
                // Remove from wishlist after purchase
                wishlistViewModel.removeFromWishlist(product.id)
                wishlistedProducts.remove(product.id)
                
                // Reset selected product
                selectedProduct = null
                
                // Update UI to reflect removal
                val currentProducts = productViewModel.products.value ?: emptyList()
                updateLikedProducts(currentProducts)
            },
            onError = { errorMessage ->
                // UI updates on error
                progressBar.visibility = View.GONE
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun toggleWishlist(product: Product) {
        if (wishlistedProducts.contains(product.id)) {
            // Show confirmation dialog for removing from wishlist
            DialogUtils.showCustomConfirmationDialog(
                context = this,
                title = "Remove from wishlist",
                message = "are you sure you want to remove this item?",
                onConfirm = {
                    wishlistViewModel.removeFromWishlist(product.id)
                    wishlistedProducts.remove(product.id)
                    (recyclerView.adapter as? ProductAdapter)?.updateWishlistState(product)
                    Toast.makeText(this, "Removed from wishlist", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            // This shouldn't happen in the likes screen, but handle it anyway
            wishlistViewModel.addToWishlist(product.id)
            wishlistedProducts.add(product.id)
            (recyclerView.adapter as? ProductAdapter)?.updateWishlistState(product)
            Toast.makeText(this, "Added to wishlist", Toast.LENGTH_SHORT).show()
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
        
        // Refresh wishlist data with correct user ID
        val app = application as? CredigoApp
        if (app?.loggedInuser != null && currentUserId <= 0) {
            // Update currentUserId if it wasn't properly set
            currentUserId = app.loggedInuser!!.id.toLong()
            wishlistViewModel.setCurrentUser(currentUserId.toInt())
        } else {
            // Just refresh the wishlist
            wishlistViewModel.loadUserWishlist()
        }
    }
}
