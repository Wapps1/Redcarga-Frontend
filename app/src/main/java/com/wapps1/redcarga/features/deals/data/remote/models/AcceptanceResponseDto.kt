package com.wapps1.redcarga.features.deals.data.remote.models

import com.squareup.moshi.Json

/**
 * DTO para response de proponer aceptación
 * POST /api/deals/quotes/{quoteId}/acceptances
 */
data class AcceptanceResponseDto(
    @Json(name = "acceptanceId")
    val acceptanceId: Int
)

