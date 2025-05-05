package com.sia.credigo.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.sia.credigo.R

object DialogUtils {
    fun showCustomConfirmationDialog(
        context: Context,
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_confirmation, null)
        dialog.setContentView(view)

        // Set dialog title and message
        view.findViewById<TextView>(R.id.dialog_title).text = title
        view.findViewById<TextView>(R.id.dialog_message).text = message

        // Set up confirm button
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btn_confirm)
        btnConfirm.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }

        // Set up cancel button
        val btnCancel = view.findViewById<MaterialButton>(R.id.btn_cancel)
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
