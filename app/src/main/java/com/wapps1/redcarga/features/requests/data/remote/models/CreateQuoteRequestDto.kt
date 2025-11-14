package com.wapps1.redcarga.features.requests.data.remote.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateQuoteRequestDto(
    val requestId: Long,
    val companyId: Long,
    val totalAmount: Double, // Backend usa double
    val currency: String,
    val items: List<QuoteItemRequestDto>
)

@JsonClass(generateAdapter = true)
data class QuoteItemRequestDto(
    val requestItemId: Long,
    val qty: Double // Backend usa double
)

