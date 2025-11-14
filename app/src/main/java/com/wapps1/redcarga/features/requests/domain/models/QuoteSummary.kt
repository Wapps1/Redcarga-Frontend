package com.wapps1.redcarga.features.requests.domain.models

import java.time.Instant

/**
 * Modelo de dominio para el resumen de una cotización
 */
data class QuoteSummary(
    val quoteId: Long,
    val requestId: Long,
    val companyId: Long,
    val totalAmount: Double,
    val currencyCode: String,
    val createdAt: Instant
)

