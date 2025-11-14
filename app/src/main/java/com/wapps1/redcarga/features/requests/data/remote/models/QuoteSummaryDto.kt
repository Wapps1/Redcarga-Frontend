package com.wapps1.redcarga.features.requests.data.remote.models

import com.squareup.moshi.JsonClass

/**
 * DTO para el resumen de cotizaciones
 * Endpoint: GET /api/deals/quotes/general?company_id={id}
 */
@JsonClass(generateAdapter = true)
data class QuoteSummaryDto(
    val quoteId: Long,
    val requestId: Long,
    val companyId: Long,
    val totalAmount: Double,
    val currencyCode: String,
    val createdAt: String // ISO 8601
)

