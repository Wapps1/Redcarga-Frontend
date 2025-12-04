package com.wapps1.redcarga.features.deals.data.remote.models

import com.squareup.moshi.Json

/**
 * DTO para request de decisión sobre un cambio propuesto
 * POST /api/deals/quotes/{quoteId}/changes/{changeId}/decision
 * 
 * Usado en estado ACEPTADA para aceptar o rechazar una propuesta de cambio
 */
data class ChangeDecisionRequestDto(
    @Json(name = "accept")
    val accept: Boolean  // true = aceptar, false = rechazar
)

