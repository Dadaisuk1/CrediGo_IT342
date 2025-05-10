package com.sia.credigo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sia.credigo.R
import com.sia.credigo.model.Product
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import android.util.Log

class ProductAdapter(
    private val products: List<Product>,
    private val onProductSelected: (Product) -> Unit,
    private val onWishlistClicked: (Product) -> Unit,
    private val isInWishlist: (Product) -> Boolean
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private var selectedProductId: Int? = null

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val vpAmountView: TextView = view.findViewById(R.id.tv_vp_amount)
        val priceView: TextView = view.findViewById(R.id.tv_price)
        val heartIcon: ImageView = view.findViewById(R.id.iv_heart)
        val productImage: ImageView = view.findViewById(R.id.iv_product_image)
        val cardView: View = view

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val product = products[position]
                    // Always notify the activity through onProductSelected
                    onProductSelected(product)
                }
            }

            // Set up heart icon click event
            heartIcon.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val product = products[position]
                    Log.d("ProductAdapter", "Wishlist clicked for product: ${product.id} at position $position")
                    onWishlistClicked(product)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        
        // Set product info with explicit visibility
        holder.vpAmountView.visibility = View.VISIBLE
        holder.vpAmountView.text = product.name ?: "No Name"

        holder.priceView.visibility = View.VISIBLE
        holder.priceView.text = "₱${product.price}"

        // Load product image
        holder.productImage.visibility = View.VISIBLE
        
        // Determine which image to load
        val imageUrl = product.imageUrl
        val context = holder.productImage.context
        
        if (!imageUrl.isNullOrEmpty()) {
            // Use Glide to load the image from URL
            Glide.with(context)
                .load(imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.img_loading)
                    .error(getDefaultImageForProduct(product)))
                .into(holder.productImage)
        } else {
            // Load default image based on product type/name
            holder.productImage.setImageResource(getDefaultImageForProduct(product))
        }

        // Update selection state using strict equality check between IDs
        val isSelected = selectedProductId != null && selectedProductId == product.id
        
        // Apply the proper background based on selection state
        holder.cardView.setBackgroundResource(
            if (isSelected) R.drawable.product_card_selected_green
            else R.drawable.product_card_background
        )

        // Ensure heart icon is visible and set correctly for each item
        holder.heartIcon.visibility = View.VISIBLE
        holder.heartIcon.setImageResource(
            if (isInWishlist(product)) R.drawable.ic_heart_fill_pink
            else R.drawable.ic_heart_line
        )
    }

    /**
     * Get default image resource based on product name/type
     */
    private fun getDefaultImageForProduct(product: Product): Int {
        return when {
            product.name?.contains("COD", ignoreCase = true) == true || 
            product.name?.contains("Call of Duty", ignoreCase = true) == true -> R.drawable.img_cod
            
            product.name?.contains("Mobile Legends", ignoreCase = true) == true || 
            product.name?.contains("ML", ignoreCase = true) == true -> R.drawable.img_ml_bg
            
            product.name?.contains("Valorant", ignoreCase = true) == true || 
            product.name?.contains("Valo", ignoreCase = true) == true -> R.drawable.img_valo_bg
            
            product.name?.contains("Genshin", ignoreCase = true) == true -> R.drawable.img_genshin_bg
            
            product.name?.contains("PUBG", ignoreCase = true) == true -> R.drawable.img_pubg_bg
            
            else -> R.drawable.img_notfound
        }
    }

    override fun getItemCount() = products.size

    fun setSelectedProduct(product: Product?) {
        // Update the selectedProductId
        val oldSelectedId = selectedProductId
        selectedProductId = product?.id
        
        // Log the selection change
        Log.d("ProductAdapter", "Setting selected product ID to: $selectedProductId")
        
        // Only notify the changed items for more efficient updates
        if (oldSelectedId != null) {
            val oldPosition = products.indexOfFirst { it.id == oldSelectedId }
            if (oldPosition != -1) {
                notifyItemChanged(oldPosition)
            }
        }
        
        if (selectedProductId != null) {
            val newPosition = products.indexOfFirst { it.id == selectedProductId }
            if (newPosition != -1) {
                notifyItemChanged(newPosition)
            }
        }
    }

    fun updateWishlistState(product: Product) {
        val position = products.indexOfFirst { it.id == product.id }
        if (position != -1) {
            Log.d("ProductAdapter", "Updating wishlist state for product ${product.id} at position $position")
            notifyItemChanged(position)
        } else {
            Log.e("ProductAdapter", "Failed to find product ${product.id} to update wishlist state")
        }
    }

    fun getSelectedProduct(): Product? {
        // Find the product by ID
        return selectedProductId?.let { id ->
            products.find { it.id == id }
        }
    }

    // Unselect product
    fun clearSelectedProduct() {
        selectedProductId = null
        notifyDataSetChanged()
    }
} 
