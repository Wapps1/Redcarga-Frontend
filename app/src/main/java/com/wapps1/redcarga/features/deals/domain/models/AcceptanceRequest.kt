package com.wapps1.redcarga.features.deals.domain.models

/**
 * Request para proponer aceptación de una cotización
 * POST /api/deals/quotes/{quoteId}/acceptances
 */
data class AcceptanceRequest(
    val idempotencyKey: String?,  // Opcional (máx 64 chars)
    val note: String?              // Opcional (máx 2000 chars)
) {
    fun isValid(): Boolean {
        // Validar longitud de idempotencyKey
        if (idempotencyKey != null && idempotencyKey.length > 64) {
            return false
        }
        // Validar longitud de note
        if (note != null && note.length > 2000) {
            return false
        }
        return true
    }
}

