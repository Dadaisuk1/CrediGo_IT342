package com.sia.credigo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sia.credigo.R
import com.sia.credigo.model.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(
    private val transactions: List<Transaction>,
    private val onTransactionClicked: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productCategoryView: TextView = view.findViewById(R.id.tv_product_category)
        val productNameView: TextView = view.findViewById(R.id.tv_product_name)
        val priceView: TextView = view.findViewById(R.id.tv_price)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTransactionClicked(transactions[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        // Parse the transaction type to extract product name and category if possible
        val parts = transaction.type.split(" - ")

        if (parts.size > 1) {
            // If the transaction type contains a separator, use that information
            holder.productNameView.text = parts[0]
            holder.productCategoryView.text = parts[1]
        } else {
            // If no separator, just use the type as product name and set a default category
            holder.productNameView.text = transaction.type
            holder.productCategoryView.text = "Game Purchase"
        }

        // Try to format and display timestamp if available
        try {
            transaction.timestamp?.let {
                val dateInfo = "• $it"
                holder.productCategoryView.text = "${holder.productCategoryView.text} $dateInfo"
            }
        } catch (e: Exception) {
            // Ignore timestamp formatting errors
        }

        // Format price with 2 decimal places
        holder.priceView.text = "₱${String.format("%.2f", transaction.amount)}"
        holder.priceView.setTextColor(holder.priceView.context.getColor(R.color.green))
    }

    override fun getItemCount() = transactions.size
}
