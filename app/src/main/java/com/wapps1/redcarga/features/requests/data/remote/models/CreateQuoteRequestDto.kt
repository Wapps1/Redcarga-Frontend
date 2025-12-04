package com.wapps1.redcarga.features.requests.data.remote.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateQuoteRequestDto(
    val requestId: Int, // ⭐ Cambiado de Long a Int para coincidir con backend (Integer)
    val companyId: Int, // ⭐ Cambiado de Long a Int para coincidir con backend (Integer)
    val totalAmount: Double, // Backend usa double
    val currency: String, // ✅ Backend acepta "PEN" como String y lo convierte a Currency enum
    val items: List<QuoteItemRequestDto>
)

@JsonClass(generateAdapter = true)
data class QuoteItemRequestDto(
    val requestItemId: Int, // ⭐ Cambiado de Long a Int para coincidir con backend (Integer)
    val qty: Double // Backend usa double
)

