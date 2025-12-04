package com.wapps1.redcarga.features.deals.data.remote.models

import com.squareup.moshi.Json

/**
 * DTO para un item de cambio
 */
data class ChangeItemDto(
    @Json(name = "changeItemId")
    val changeItemId: Int?,  // ⚠️ Nullable: presente en mensajes de chat, no en requests
    
    @Json(name = "fieldCode")
    val fieldCode: String,  // "PRICE_TOTAL", "QTY", "ITEM_REMOVE", "ITEM_ADD"
    
    @Json(name = "targetQuoteItemId")
    val targetQuoteItemId: Int?,  // null para PRICE_TOTAL, requerido para QTY e ITEM_REMOVE
    
    @Json(name = "targetRequestItemId")
    val targetRequestItemId: Int?,  // null para TRATO, requerido para ITEM_ADD en ACEPTADA
    
    @Json(name = "oldValue")
    val oldValue: String?,  // Opcional
    
    @Json(name = "newValue")
    val newValue: String?   // Requerido para PRICE_TOTAL y QTY, null para ITEM_REMOVE, opcional para ITEM_ADD
)

