package com.sia.credigo.model

data class Platform(
    val id: Int = 0,
    val name: String = "",
    val description: String? = null,
    val iconUrl: String? = null,
    val logoUrl: String? = null,
    val isActive: Boolean = true
)
