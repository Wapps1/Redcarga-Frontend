package com.wapps1.redcarga.features.requests.data.remote.models

import com.squareup.moshi.JsonClass

/**
 * DTO para el detalle completo de una cotización
 * Endpoint: GET /api/deals/quotes/{quoteId}/detail
 */
@JsonClass(generateAdapter = true)
data class QuoteDetailDto(
    val quoteId: Long,
    val requestId: Long,
    val companyId: Long,
    val createdByAccountId: Long,
    val stateCode: String,
    val currencyCode: String,
    val totalAmount: Double,
    val version: Int,
    val createdAt: String, // ISO 8601
    val updatedAt: String, // ISO 8601
    val items: List<QuoteItemDto>
)

@JsonClass(generateAdapter = true)
data class QuoteItemDto(
    val quoteItemId: Long,
    val requestItemId: Long,
    val qty: Int,
    val version: Int
)

