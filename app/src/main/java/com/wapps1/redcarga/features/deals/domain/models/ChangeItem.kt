package com.wapps1.redcarga.features.deals.domain.models

/**
 * Item individual de un cambio
 * Usado en estado TRATO para aplicar cambios inmediatamente
 * Usado en estado ACEPTADA para proponer cambios que requieren aprobación
 */
data class ChangeItem(
    val changeItemId: Long?,              // ⚠️ Nullable: presente en mensajes de chat, no en requests
    val fieldCode: ChangeFieldCode,        // PRICE_TOTAL, QTY, ITEM_REMOVE
    val targetQuoteItemId: Long?,         // ID del quote_item (requerido para QTY e ITEM_REMOVE)
    val targetRequestItemId: Long?,        // No se usa (siempre null)
    val oldValue: String?,                // Valor anterior (opcional)
    val newValue: String?                 // Valor nuevo (requerido para PRICE_TOTAL y QTY)
)

/**
 * Código del campo que se modifica
 */
enum class ChangeFieldCode {
    PRICE_TOTAL,    // Cambiar monto total
    QTY,            // Cambiar cantidad de un item
    ITEM_REMOVE,    // Eliminar un item
    ITEM_ADD        // Agregar un item (usado en ACEPTADA)
}

