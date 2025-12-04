package com.wapps1.redcarga.features.deals.data.remote.models

import com.squareup.moshi.Json

/**
 * DTO para response de confirmar/rechazar aceptación
 * POST /api/deals/quotes/{quoteId}/acceptances/{acceptanceId}/confirm
 * POST /api/deals/quotes/{quoteId}/acceptances/{acceptanceId}/reject
 */
data class ConfirmAcceptanceResponseDto(
    @Json(name = "ok")
    val ok: Boolean
)

