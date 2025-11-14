package com.wapps1.redcarga.features.requests.domain.models

import java.time.Instant

/**
 * Modelo de dominio para el detalle completo de una cotización
 */
data class QuoteDetail(
    val quoteId: Long,
    val requestId: Long,
    val companyId: Long,
    val createdByAccountId: Long,
    val stateCode: String,
    val currencyCode: String,
    val totalAmount: Double,
    val version: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val items: List<QuoteItem>
)

data class QuoteItem(
    val quoteItemId: Long,
    val requestItemId: Long,
    val qty: Int,
    val version: Int
)

