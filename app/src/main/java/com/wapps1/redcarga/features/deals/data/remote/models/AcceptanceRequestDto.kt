package com.wapps1.redcarga.features.deals.data.remote.models

import com.squareup.moshi.Json

/**
 * DTO para request de proponer aceptación
 * POST /api/deals/quotes/{quoteId}/acceptances
 */
data class AcceptanceRequestDto(
    @Json(name = "idempotencyKey")
    val idempotencyKey: String?,  // Opcional (máx 64 chars)
    
    @Json(name = "note")
    val note: String?  // Opcional (máx 2000 chars)
)

