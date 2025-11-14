package com.wapps1.redcarga.features.requests.domain.models

import java.math.BigDecimal

/**
 * Request para crear una cotización
 */
data class CreateQuoteRequest(
    val requestId: Long,
    val companyId: Long,
    val totalAmount: BigDecimal,
    val currency: String = "PEN",
    val items: List<QuoteItemRequest>
)

/**
 * Item individual de la cotización
 */
data class QuoteItemRequest(
    val requestItemId: Long,
    val qty: BigDecimal
)

