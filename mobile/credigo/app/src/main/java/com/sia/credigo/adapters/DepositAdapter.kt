package com.sia.credigo.adapters

import com.sia.credigo.model.Transaction
import com.sia.credigo.model.TransactionType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sia.credigo.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DepositAdapter(private val transactions: List<Transaction>) : 
    RecyclerView.Adapter<DepositAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val paymentMethodIcon: ImageView = view.findViewById(R.id.iv_payment_method)
        val paymentMethodText: TextView = view.findViewById(R.id.tv_payment_method)
        val dateTimeText: TextView = view.findViewById(R.id.tv_date_time)
        val amountText: TextView = view.findViewById(R.id.tv_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deposit, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]

        // Set payment method icon based on type
        val paymentOption = transaction.type // Using type field for payment option
        val iconResource = when (paymentOption) {
            "GCash" -> R.drawable.logo_gcash
            "Maya" -> R.drawable.logo_paymaya
            "Visa" -> R.drawable.logo_visa2
            "Mastercard" -> R.drawable.logo_mastercard
            else -> R.drawable.ic_payment_default
        }
        holder.paymentMethodIcon.setImageResource(iconResource)

        // Set payment method name
        holder.paymentMethodText.text = paymentOption

        // Format and set date/time
        try {
            // Format timestamp to a readable date
            val date = Date(transaction.timestamp)
            val outputFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
            holder.dateTimeText.text = outputFormat.format(date)
        } catch (e: Exception) {
            // If date formatting fails, show a default message
            holder.dateTimeText.text = "N/A"
        }

        // Format and set amount
        holder.amountText.text = String.format("₱%,.2f", transaction.amount)
    }

    override fun getItemCount() = transactions.size
} 
