package com.sia.credigo.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sia.credigo.ProductsListActivity
import com.sia.credigo.R
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.model.Platform
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class GameCategoryAdapter(private val platforms: List<Platform>) : 
    RecyclerView.Adapter<GameCategoryAdapter.GameCategoryViewHolder>() {
    
    private val TAG = "GameCategoryAdapter"

    init {
        Log.d(TAG, "Adapter initialized with ${platforms.size} platforms")
    }

    class GameCategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.iv_game_image)
        val titleView: TextView = view.findViewById(R.id.tv_game_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameCategoryViewHolder {
        Log.d(TAG, "Creating view holder")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game_category, parent, false)
        return GameCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameCategoryViewHolder, position: Int) {
        val platform = platforms[position]
        Log.d(TAG, "Binding platform: ${platform.name} at position $position")

        // Set platform name
        holder.titleView.text = platform.name
        holder.titleView.visibility = View.VISIBLE

        // Special handling for drawable:// URLs
        if (platform.logoUrl?.startsWith("drawable://") == true) {
            val resourceName = platform.logoUrl.removePrefix("drawable://")
            Log.d(TAG, "Loading image from drawable resource: $resourceName")
            
            val resId = holder.itemView.context.resources.getIdentifier(
                resourceName, 
                "drawable", 
                holder.itemView.context.packageName
            )
            
            if (resId != 0) {
                holder.imageView.setImageResource(resId)
                Log.d(TAG, "Successfully loaded drawable resource: $resourceName")
            } else {
                Log.e(TAG, "Resource not found: $resourceName, using fallback")
                holder.imageView.setImageResource(R.drawable.img_notfound)
                tryLoadByPlatformName(holder, platform)
            }
        }
        // Load image with Glide if logoUrl is valid
        else if (!platform.logoUrl.isNullOrEmpty() && (platform.logoUrl.startsWith("http") || platform.logoUrl.startsWith("https"))) {
            Log.d(TAG, "Loading image from URL: ${platform.logoUrl}")
            try {
                Glide.with(holder.itemView.context)
                    .load(platform.logoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.img_notfound)
                    .error(R.drawable.img_notfound)
                    .into(holder.imageView)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading image from URL for ${platform.name}: ${e.message}")
                tryLoadByPlatformName(holder, platform)
            }
        } else {
            // Try to load from local resources
            Log.d(TAG, "No valid URL, trying to load from local resources")
            tryLoadByPlatformName(holder, platform)
        }

        // Set click listener
        holder.itemView.setOnClickListener {
            Log.d(TAG, "Platform clicked: ${platform.name}, ID: ${platform.id}")
            val context = holder.itemView.context
            val userId = (context.applicationContext as CredigoApp).loggedInuser?.userid ?: -1

            val intent = Intent(context, ProductsListActivity::class.java).apply {
                putExtra("CATEGORY_ID", platform.id.toLong())
                putExtra("CATEGORY_NAME", platform.name)
                putExtra("USER_ID", userId.toLong())
            }
            context.startActivity(intent)
        }
    }

    private fun tryLoadByPlatformName(holder: GameCategoryViewHolder, platform: Platform) {
        try {
            val context = holder.itemView.context
            
            // Try multiple image naming variations
            val options = listOf(
                // Direct name
                platform.name.lowercase().replace(" ", "_"),
                // Common abbreviations
                getAbbreviation(platform.name),
                // With img_ prefix
                "img_${platform.name.lowercase().replace(" ", "_")}",
                // With img_ prefix and abbreviation
                "img_${getAbbreviation(platform.name)}"
            )
            
            Log.d(TAG, "Trying resource name options: $options")
            
            for (option in options) {
                val resId = context.resources.getIdentifier(
                    option, 
                    "drawable", 
                    context.packageName
                )
                
                if (resId != 0) {
                    Log.d(TAG, "Found resource for ${platform.name}: $option")
                    holder.imageView.setImageResource(resId)
                    return
                }
            }
            
            // If we get here, none of the attempts worked
            Log.w(TAG, "No resource found for ${platform.name}, using fallback image")
            holder.imageView.setImageResource(R.drawable.img_notfound)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading local image for ${platform.name}: ${e.message}")
            holder.imageView.setImageResource(R.drawable.img_notfound)
        }
    }
    
    private fun getAbbreviation(name: String): String {
        // Return known abbreviations for popular games
        return when (name.lowercase()) {
            "mobile legends" -> "ml"
            "league of legends" -> "lol"
            "valorant" -> "valo"
            "call of duty" -> "cod"
            "wild rift" -> "wildrift"
            "pubg mobile" -> "pubg"
            "genshin impact" -> "genshin"
            "zenless zone zero" -> "zenless"
            "honkai star rail" -> "star_rail"
            else -> name.lowercase().replace(" ", "_")
        }
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: ${platforms.size}")
        return platforms.size
    }
}
