package com.wapps1.redcarga.features.deals.domain.models

/**
 * Response al proponer aceptación
 * POST /api/deals/quotes/{quoteId}/acceptances
 */
data class AcceptanceResponse(
    val acceptanceId: Long  // ID de la propuesta creada
)

