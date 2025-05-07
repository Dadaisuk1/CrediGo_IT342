package com.sia.credigo.adapters

import android.content.Intent
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

class GameCategoryAdapter(private val platforms: List<Platform>) : 
    RecyclerView.Adapter<GameCategoryAdapter.GameCategoryViewHolder>() {

    class GameCategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.iv_game_image)
        val titleView: TextView = view.findViewById(R.id.tv_game_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameCategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game_category, parent, false)
        return GameCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameCategoryViewHolder, position: Int) {
        val platform = platforms[position]

        // Set platform name
        holder.titleView.text = platform.name
        holder.titleView.visibility = View.VISIBLE

        // Set image with proper scaling
        holder.imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        try {
            // Try to extract image name from logoUrl or use name as fallback
            val imageName = platform.logoUrl?.substringAfterLast("/")?.substringBeforeLast(".") 
                ?: platform.name.lowercase().replace(" ", "_")

            val resId = holder.imageView.context.resources.getIdentifier(imageName, "drawable", holder.imageView.context.packageName)
            if (resId != 0) {
                holder.imageView.setImageResource(resId)
            } else {
                holder.imageView.setImageResource(R.drawable.img_notfound)
            }
        } catch (e: Exception) {
            android.util.Log.e("GameCategoryAdapter", "Error loading image for ${platform.name}: ${e.message}")
            holder.imageView.setImageResource(R.drawable.img_notfound)
        }

        // Set click listener
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val userId = (context.applicationContext as CredigoApp).loggedInuser?.userid ?: -1

            val intent = Intent(context, ProductsListActivity::class.java).apply {
                putExtra("CATEGORY_ID", platform.id)
                putExtra("CATEGORY_NAME", platform.name)
                putExtra("USER_ID", userId)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = platforms.size
}
