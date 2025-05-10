package com.sia.credigo.utils

import android.content.res.ColorStateList
import android.widget.Button
import androidx.core.content.ContextCompat
import com.sia.credigo.R
import com.sia.credigo.model.Product

/**
 * Handles UI and logic for product sorting buttons
 * Now works with pre-sorted backend data
 */
class SortButtonsHandler(
    private val btnPopular: Button,
    private val btnHighLow: Button,
    private val btnLowHigh: Button,
    private val btnAZ: Button,
    private val onSortChanged: (List<Product>) -> Unit
) {
    private var currentSort: SortType = SortType.MOST_RECENT
    private var currentProducts: List<Product> = emptyList()

    enum class SortType {
        MOST_RECENT, POPULAR, HIGH_LOW, LOW_HIGH, A_Z
    }

    init {
        setupButtonListeners()
        updateButtonStates()
    }

    private fun setupButtonListeners() {
        btnPopular.setOnClickListener { handleSortChange(SortType.POPULAR) }
        btnHighLow.setOnClickListener { handleSortChange(SortType.HIGH_LOW) }
        btnLowHigh.setOnClickListener { handleSortChange(SortType.LOW_HIGH) }
        btnAZ.setOnClickListener { handleSortChange(SortType.A_Z) }
    }

    private fun handleSortChange(newSort: SortType) {
        // Only process if it's a new sort or we have products
        if (newSort != currentSort || currentProducts.isNotEmpty()) {
            currentSort = newSort
            updateButtonStates()
            
            // Apply sorting to current products
            applySortAndNotify()
        }
    }

    private fun updateButtonStates() {
        // Reset all buttons
        btnPopular.isSelected = false
        btnHighLow.isSelected = false
        btnLowHigh.isSelected = false
        btnAZ.isSelected = false
        
        // Set visual appearance for buttons
        when (currentSort) {
            SortType.POPULAR -> btnPopular.isSelected = true
            SortType.HIGH_LOW -> btnHighLow.isSelected = true
            SortType.LOW_HIGH -> btnLowHigh.isSelected = true
            SortType.A_Z -> btnAZ.isSelected = true
            else -> {} // Default/MOST_RECENT has no selection
        }
        
        // Update button background colors
        updateButtonBackgrounds()
    }
    
    private fun updateButtonBackgrounds() {
        // Get the colors from resources
        val context = btnPopular.context
        val credigoPink = ContextCompat.getColor(context, R.color.credigo_pink)
        val white = ContextCompat.getColor(context, R.color.white)
        val transparent = 0x00000000
        val black = ContextCompat.getColor(context, R.color.black)
        
        // Apply visual style to selected/unselected buttons
        val buttons = listOf(btnPopular, btnHighLow, btnLowHigh, btnAZ)
        buttons.forEach { button ->
            if (button.isSelected) {
                // Selected state - use credigo_pink background with white text
                button.setBackgroundColor(credigoPink)
                button.setTextColor(white)
                button.alpha = 1.0f
            } else {
                // Normal state - transparent background with black text
                button.setBackgroundColor(transparent)
                button.setTextColor(black)
                button.alpha = 0.7f
            }
        }
    }

    fun getCurrentSort() = currentSort

    fun updateProducts(products: List<Product>, applySort: Boolean = true) {
        // Store the current products
        currentProducts = products
        
        if (applySort) {
            // Apply current sort
            applySortAndNotify()
        } else {
            // Just pass the products without sorting
            onSortChanged(products)
        }
    }
    
    private fun applySortAndNotify() {
        // Skip if we have no products
        if (currentProducts.isEmpty()) {
            return
        }
        
        // Apply current sort to products
        val sortedProducts = when (currentSort) {
            SortType.POPULAR -> currentProducts.sortedByDescending { it.price } // Just an example, replace with actual popularity logic
            SortType.HIGH_LOW -> currentProducts.sortedByDescending { it.price }
            SortType.LOW_HIGH -> currentProducts.sortedBy { it.price }
            SortType.A_Z -> currentProducts.sortedBy { it.name }
            else -> currentProducts // MOST_RECENT or default
        }
        
        // Notify the callback with sorted results
        onSortChanged(sortedProducts)
    }
}
