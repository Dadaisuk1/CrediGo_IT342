package com.sia.credigo.network.models

data class ReviewRequest(
    val rating: Int,
    val comment: String?
)
