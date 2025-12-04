package com.wapps1.redcarga.features.deals.domain.models

/**
 * Response al confirmar/rechazar aceptación
 * POST /api/deals/quotes/{quoteId}/acceptances/{acceptanceId}/confirm
 * POST /api/deals/quotes/{quoteId}/acceptances/{acceptanceId}/reject
 */
data class ConfirmAcceptanceResponse(
    val ok: Boolean  // Siempre true si la operación fue exitosa
)

