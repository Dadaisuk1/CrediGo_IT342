package com.sia.credigo.utils

import android.widget.Button
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

    enum class SortType {
        MOST_RECENT, POPULAR, HIGH_LOW, LOW_HIGH, A_Z
    }

    init {
        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        btnPopular.setOnClickListener { handleSortChange(SortType.POPULAR) }
        btnHighLow.setOnClickListener { handleSortChange(SortType.HIGH_LOW) }
        btnLowHigh.setOnClickListener { handleSortChange(SortType.LOW_HIGH) }
        btnAZ.setOnClickListener { handleSortChange(SortType.A_Z) }
    }

    private fun handleSortChange(newSort: SortType) {
        currentSort = newSort
        updateButtonStates()
        onSortChanged(emptyList()) // Backend will handle actual sorting
    }

    private fun updateButtonStates() {
        btnPopular.isSelected = currentSort == SortType.POPULAR
        btnHighLow.isSelected = currentSort == SortType.HIGH_LOW
        btnLowHigh.isSelected = currentSort == SortType.LOW_HIGH
        btnAZ.isSelected = currentSort == SortType.A_Z
    }

    fun getCurrentSort() = currentSort

    fun updateProducts(products: List<Product>, applySort: Boolean = false) {
        if (applySort) {
            // Apply current sort to products
            val sortedProducts = when (currentSort) {
                SortType.POPULAR -> products.sortedByDescending { it.price } // Just an example, replace with actual popularity logic
                SortType.HIGH_LOW -> products.sortedByDescending { it.price }
                SortType.LOW_HIGH -> products.sortedBy { it.price }
                SortType.A_Z -> products.sortedBy { it.name }
                else -> products // MOST_RECENT or default
            }
            onSortChanged(sortedProducts)
        } else {
            // Just pass the products without sorting
            onSortChanged(products)
        }
    }
}
