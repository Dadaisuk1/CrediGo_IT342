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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.sia.credigo.R

class CardOptionFragment : Fragment() {

    // UI components
    private lateinit var cardNumberEditText: EditText
    private lateinit var monthEditText: EditText
    private lateinit var yearEditText: EditText
    private lateinit var cvcEditText: EditText
    private lateinit var nameOnCardEditText: EditText

    // Error text views
    private lateinit var cardNumberErrorText: TextView
    private lateinit var monthErrorText: TextView
    private lateinit var yearErrorText: TextView
    private lateinit var cvcErrorText: TextView
    private lateinit var nameOnCardErrorText: TextView

    // Card data class to hold card details
    data class CardDetails(
        val cardNumber: String,
        val month: String,
        val year: String,
        val cvc: String,
        val cardHolderName: String
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_card_option, container, false)

        // Initialize views
        initializeViews(view)
        
        // Set up text watchers for formatting and validation
        setupTextWatchers()

        return view
    }

    private fun initializeViews(view: View) {
        // Initialize EditTexts
        cardNumberEditText = view.findViewById(R.id.edit_card_number)
        monthEditText = view.findViewById(R.id.edit_month)
        yearEditText = view.findViewById(R.id.edit_year)
        cvcEditText = view.findViewById(R.id.edit_cvc)
        nameOnCardEditText = view.findViewById(R.id.edit_name_on_card)

        // Initialize error text views
        cardNumberErrorText = view.findViewById(R.id.text_card_number_error)
        monthErrorText = view.findViewById(R.id.text_month_error)
        yearErrorText = view.findViewById(R.id.text_year_error)
        cvcErrorText = view.findViewById(R.id.text_cvc_error)
        nameOnCardErrorText = view.findViewById(R.id.text_name_on_card_error)

        // Set max length for each field
        cardNumberEditText.filters = arrayOf(InputFilter.LengthFilter(19)) // 16 digits + 3 spaces
        monthEditText.filters = arrayOf(InputFilter.LengthFilter(2))
        yearEditText.filters = arrayOf(InputFilter.LengthFilter(2))
        cvcEditText.filters = arrayOf(InputFilter.LengthFilter(3))
        nameOnCardEditText.filters = arrayOf(InputFilter.LengthFilter(50))

        // Initially hide error messages
        hideAllErrorMessages()
    }

    private fun setupTextWatchers() {
        // Card number formatting (add spaces after every 4 digits)
        cardNumberEditText.addTextChangedListener(object : TextWatcher {
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
                // Re-add spaces every 4 characters
                val formatted = StringBuilder()
                for (i in digitsOnly.indices) {
                    if (i > 0 && i % 4 == 0) {
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
                validateCardNumber()
            }
        })

        // Month validation (1-12)
        monthEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateMonth()
            }
        })

        // Focus change validation for month
        monthEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateMonth()
            }
        }

        // Year validation
        yearEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateYear()
            }
        })

        // Focus change validation for year
        yearEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateYear()
            }
        }

        // CVC validation
        cvcEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateCvc()
            }
        })

        // Focus change validation for CVC
        cvcEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateCvc()
            }
        }

        // Name on card validation
        nameOnCardEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateNameOnCard()
            }
        })

        // Focus change validation for name on card
        nameOnCardEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateNameOnCard()
            }
        }
    }

    // Validation methods
    private fun validateCardNumber(): Boolean {
        val cardNumber = cardNumberEditText.text.toString().replace(" ", "")
        
        return when {
            cardNumber.isEmpty() -> {
                showError(cardNumberErrorText, "Card number is required")
                false
            }
            cardNumber.length < 16 -> {
                showError(cardNumberErrorText, "Card number must be 16 digits")
                false
            }
            !cardNumber.all { it.isDigit() } -> {
                showError(cardNumberErrorText, "Card number must contain only digits")
                false
            }
            else -> {
                hideError(cardNumberErrorText)
                true
            }
        }
    }

    private fun validateMonth(): Boolean {
        val month = monthEditText.text.toString()
        
        return when {
            month.isEmpty() -> {
                showError(monthErrorText, "Month is required")
                false
            }
            month.toIntOrNull() == null -> {
                showError(monthErrorText, "Invalid month")
                false
            }
            month.toInt() < 1 || month.toInt() > 12 -> {
                showError(monthErrorText, "Month must be between 1-12")
                false
            }
            else -> {
                hideError(monthErrorText)
                true
            }
        }
    }

    private fun validateYear(): Boolean {
        val year = yearEditText.text.toString()
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) % 100
        
        return when {
            year.isEmpty() -> {
                showError(yearErrorText, "Year is required")
                false
            }
            year.toIntOrNull() == null -> {
                showError(yearErrorText, "Invalid year")
                false
            }
            year.toInt() < currentYear -> {
                showError(yearErrorText, "Card is expired")
                false
            }
            else -> {
                hideError(yearErrorText)
                true
            }
        }
    }

    private fun validateCvc(): Boolean {
        val cvc = cvcEditText.text.toString()
        
        return when {
            cvc.isEmpty() -> {
                showError(cvcErrorText, "CVC is required")
                false
            }
            cvc.length < 3 -> {
                showError(cvcErrorText, "CVC must be 3 digits")
                false
            }
            !cvc.all { it.isDigit() } -> {
                showError(cvcErrorText, "CVC must contain only digits")
                false
            }
            else -> {
                hideError(cvcErrorText)
                true
            }
        }
    }

    private fun validateNameOnCard(): Boolean {
        val name = nameOnCardEditText.text.toString()
        
        return when {
            name.isEmpty() -> {
                showError(nameOnCardErrorText, "Name on card is required")
                false
            }
            name.length < 3 -> {
                showError(nameOnCardErrorText, "Name is too short")
                false
            }
            else -> {
                hideError(nameOnCardErrorText)
                true
            }
        }
    }

    private fun showError(errorTextView: TextView, message: String) {
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
    }

    private fun hideError(errorTextView: TextView) {
        errorTextView.visibility = View.GONE
    }

    private fun hideAllErrorMessages() {
        cardNumberErrorText.visibility = View.GONE
        monthErrorText.visibility = View.GONE
        yearErrorText.visibility = View.GONE
        cvcErrorText.visibility = View.GONE
        nameOnCardErrorText.visibility = View.GONE
    }

    // Public method to validate all fields at once (called from WalletActivity)
    fun validateAllFields(): Boolean {
        val cardNumberValid = validateCardNumber()
        val monthValid = validateMonth()
        val yearValid = validateYear()
        val cvcValid = validateCvc()
        val nameValid = validateNameOnCard()
        
        return cardNumberValid && monthValid && yearValid && cvcValid && nameValid
    }

    // Public method to get card details (called from WalletActivity)
    fun getCardDetails(): CardDetails {
        return CardDetails(
            cardNumber = cardNumberEditText.text.toString().replace(" ", ""),
            month = monthEditText.text.toString(),
            year = yearEditText.text.toString(),
            cvc = cvcEditText.text.toString(),
            cardHolderName = nameOnCardEditText.text.toString()
        )
    }
} 
} 