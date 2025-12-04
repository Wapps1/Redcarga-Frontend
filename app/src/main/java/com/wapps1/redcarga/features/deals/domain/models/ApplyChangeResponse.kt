package com.wapps1.redcarga.features.deals.domain.models

/**
 * Response al aplicar cambios en estado TRATO
 * En TRATO siempre retorna changeId (nunca null)
 */
data class ApplyChangeResponse(
    val changeId: Long  // ID del cambio creado (siempre presente en TRATO)
)

