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

class ProductAdapter(
    private val products: List<Product>,
    private val onProductSelected: (Product) -> Unit,
    private val onWishlistClicked: (Product) -> Unit,
    private val isInWishlist: (Product) -> Boolean
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private var selectedProduct: Product? = null

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

            heartIcon.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val product = products[position]
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
        val isSelected = product == selectedProduct

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

        // Update selection state
        holder.cardView.setBackgroundResource(
            if (selectedProduct?.productid == product.productid) R.drawable.product_card_selected_green
            else R.drawable.product_card_background
        )

        // Ensure heart icon is visible
        holder.heartIcon.visibility = View.VISIBLE
        holder.heartIcon.setImageResource(
            if (isInWishlist(product)) R.drawable.ic_heart_green
            else R.drawable.ic_heart
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
        selectedProduct = product
        notifyDataSetChanged()
    }

    fun updateWishlistState(product: Product) {
        val position = products.indexOfFirst { it.productid == product.productid }
        if (position != -1) {
            notifyItemChanged(position)
        }
    }

    fun getSelectedProduct(): Product? = selectedProduct

    // Unselect product
    fun clearSelectedProduct() {
        selectedProduct = null
        notifyDataSetChanged()
    }
} 
