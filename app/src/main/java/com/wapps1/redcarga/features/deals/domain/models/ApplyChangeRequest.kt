package com.wapps1.redcarga.features.deals.domain.models

/**
 * Request para aplicar cambios a una cotización
 * En TRATO: Los cambios se aplican inmediatamente (kind=LIBRE, status=APLICADO)
 * En ACEPTADA: Crea una propuesta de cambio (kind=PROPUESTA, status=PENDIENTE)
 */
data class ApplyChangeRequest(
    val items: List<ChangeItem>
) {
    fun isValid(): Boolean {
        if (items.isEmpty()) return false
        
        return items.all { item ->
            when (item.fieldCode) {
                ChangeFieldCode.PRICE_TOTAL -> {
                    // Requiere newValue, no requiere targetQuoteItemId
                    item.newValue != null && 
                    item.newValue.isNotBlank() &&
                    item.targetQuoteItemId == null
                }
                ChangeFieldCode.QTY -> {
                    // Requiere newValue y targetQuoteItemId
                    item.newValue != null && 
                    item.newValue.isNotBlank() &&
                    item.targetQuoteItemId != null
                }
                ChangeFieldCode.ITEM_REMOVE -> {
                    // Requiere targetQuoteItemId, no requiere newValue
                    item.targetQuoteItemId != null &&
                    item.newValue == null
                }
                ChangeFieldCode.ITEM_ADD -> {
                    // Requiere targetRequestItemId, newValue es opcional (default 1.0)
                    item.targetRequestItemId != null &&
                    (item.newValue == null || item.newValue.isNotBlank())
                }
            }
        }
    }
}

