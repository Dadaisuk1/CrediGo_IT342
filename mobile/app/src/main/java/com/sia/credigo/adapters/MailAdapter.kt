package com.sia.credigo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sia.credigo.R
import com.sia.credigo.model.Mail

class MailAdapter(
    private val mails: List<Mail>,
    private val onMailClicked: (Mail) -> Unit,
    private val onMailLongClicked: ((Mail) -> Unit)? = null
) : RecyclerView.Adapter<MailAdapter.MailViewHolder>() {

    inner class MailViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val subjectView: TextView = view.findViewById(R.id.tv_mail_subject)
        val unreadIndicator: ImageView = view.findViewById(R.id.iv_unread_indicator)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onMailClicked(mails[position])
                }
            }

            view.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && onMailLongClicked != null) {
                    onMailLongClicked.invoke(mails[position])
                    return@setOnLongClickListener true
                }
                return@setOnLongClickListener false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mail, parent, false)
        return MailViewHolder(view)
    }

    override fun onBindViewHolder(holder: MailViewHolder, position: Int) {
        val mail = mails[position]

        // Set mail info
        holder.subjectView.text = mail.subject

        // Show/hide unread indicator
        holder.unreadIndicator.visibility = if (!mail.isRead) View.VISIBLE else View.GONE
    }
    override fun getItemCount() = mails.size
}
