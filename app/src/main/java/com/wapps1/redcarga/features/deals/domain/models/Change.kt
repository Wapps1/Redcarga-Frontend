package com.wapps1.redcarga.features.deals.domain.models

import java.time.Instant

/**
 * Cambio aplicado a una cotización
 * En TRATO: siempre kind=LIBRE, status=APLICADO
 * En ACEPTADA: kind=PROPUESTA, status=PENDIENTE | APLICADO | RECHAZADO
 * Este modelo se usa principalmente para parsear mensajes de chat (CHANGE_APPLIED, CHANGE_PROPOSED, CHANGE_ACCEPTED, CHANGE_REJECTED)
 */
data class Change(
    val changeId: Long,
    val quoteId: Long?,                   // ⚠️ Nullable: presente en mensajes de chat
    val kindCode: ChangeKind,              // LIBRE (en TRATO)
    val statusCode: ChangeStatus,         // APLICADO (en TRATO)
    val createdBy: Long,                   // accountId del creador
    val createdAt: Instant,
    val items: List<ChangeItem>           // Lista de cambios
)

/**
 * Tipo de cambio
 */
enum class ChangeKind {
    LIBRE,         // Cambio libre (usado en TRATO/EN_ESPERA)
    PROPUESTA      // Propuesta de cambio (usado en ACEPTADA)
}

/**
 * Estado del cambio
 */
enum class ChangeStatus {
    APLICADO,      // Cambio aplicado (usado en TRATO)
    PENDIENTE,     // Cambio pendiente (usado en ACEPTADA)
    RECHAZADO      // Cambio rechazado (usado en ACEPTADA)
}

