package com.sia.credigo.fragments

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sia.credigo.R

class EWalletOptionFragment : Fragment() {
    
    // UI Components
    private lateinit var phoneNumberEditText: EditText
    private lateinit var phoneNumberErrorText: TextView
    private lateinit var walletInstructionsTitle: TextView
    private lateinit var walletInstructionsText: TextView
    
    // Current wallet type (GCash, Maya, etc.)
    private var walletType: String = "GCash"
    
    companion object {
        private const val ARG_WALLET_TYPE = "wallet_type"
        
        fun newInstance(walletType: String): EWalletOptionFragment {
            val fragment = EWalletOptionFragment()
            val args = Bundle()
            args.putString(ARG_WALLET_TYPE, walletType)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            walletType = it.getString(ARG_WALLET_TYPE, "GCash")
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_ewallet_option, container, false)
        
        // Initialize views
        initializeViews(view)
        
        // Set up text watchers for formatting and validation
        setupTextWatchers()
        
        // Update instructions based on wallet type
        updateWalletInstructions()

        return view
    }
    
    private fun initializeViews(view: View) {
        // Initialize EditText
        phoneNumberEditText = view.findViewById(R.id.edit_phone_number)
        
        // Initialize error text
        phoneNumberErrorText = view.findViewById(R.id.text_phone_number_error)
        
        // Initialize instruction text views
        walletInstructionsTitle = view.findViewById(R.id.text_wallet_instructions_title)
        walletInstructionsText = view.findViewById(R.id.text_wallet_instructions)
        
        // Set maximum length for phone number (11 digits + 2 spaces = 13 characters)
        phoneNumberEditText.filters = arrayOf(InputFilter.LengthFilter(13))
        
        // Initially hide error message
        phoneNumberErrorText.visibility = View.GONE
    }
    
    private fun setupTextWatchers() {
        // Phone number formatting (format: 09XX XXX XXXX)
        phoneNumberEditText.addTextChangedListener(object : TextWatcher {
            var isFormatting = false
            var previousText = ""
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousText = s.toString()
            }
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true
                
                // Remove all spaces first
                val digitsOnly = s.toString().replace(" ", "")
                
                // Re-add spaces in the format: 09XX XXX XXXX
                val formatted = StringBuilder()
                for (i in digitsOnly.indices) {
                    if (i == 4 || i == 7) {
                        formatted.append(" ")
                    }
                    formatted.append(digitsOnly[i])
                }
                
                // Only update if different to avoid infinite loop
                if (formatted.toString() != s.toString()) {
                    s?.replace(0, s.length, formatted)
                }
                
                isFormatting = false
                
                // Validate after formatting
                validatePhoneNumber()
            }
        })
        
        // Focus change validation
        phoneNumberEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validatePhoneNumber()
            }
        }
    }
    
    private fun validatePhoneNumber(): Boolean {
        val phoneNumber = phoneNumberEditText.text.toString().replace(" ", "")
        
        return when {
            phoneNumber.isEmpty() -> {
                showError("Phone number is required")
                false
            }
            phoneNumber.length < 11 -> {
                showError("Phone number must be 11 digits")
                false
            }
            !phoneNumber.startsWith("09") -> {
                showError("Phone number must start with 09")
                false
            }
            !phoneNumber.all { it.isDigit() } -> {
                showError("Phone number must contain only digits")
                false
            }
            else -> {
                hideError()
                true
            }
        }
    }
    
    private fun showError(message: String) {
        phoneNumberErrorText.text = message
        phoneNumberErrorText.visibility = View.VISIBLE
    }
    
    private fun hideError() {
        phoneNumberErrorText.visibility = View.GONE
    }
    
    /**
     * Updates the instructions based on the wallet type
     */
    private fun updateWalletInstructions() {
        walletInstructionsTitle.text = "$walletType Instructions:"
        
        val instructions = """
            1. Enter your $walletType-registered mobile number
            2. Click "Top-up Now" to generate a payment link
            3. You'll be redirected to $walletType to complete the payment
            4. After successful payment, your CrediGo wallet will be credited
        """.trimIndent()
        
        walletInstructionsText.text = instructions
    }
    
    /**
     * Public method to set wallet type and update UI accordingly
     */
    fun setWalletType(type: String) {
        walletType = type
        if (view != null) {
            updateWalletInstructions()
        }
    }
    
    /**
     * Public method to validate all fields (called from WalletActivity)
     */
    fun validateAllFields(): Boolean {
        return validatePhoneNumber()
    }
    
    /**
     * Public method to get phone number (called from WalletActivity)
     */
    fun getPhoneNumber(): String {
        return phoneNumberEditText.text.toString().replace(" ", "")
    }
} 