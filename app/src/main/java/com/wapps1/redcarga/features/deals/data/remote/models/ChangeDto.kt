package com.wapps1.redcarga.features.deals.data.remote.models

import com.squareup.moshi.Json

/**
 * DTO para un cambio completo
 * Usado principalmente para parsear mensajes de chat (CHANGE_APPLIED, CHANGE_PROPOSED, CHANGE_ACCEPTED, CHANGE_REJECTED)
 * En TRATO: kindCode="LIBRE", statusCode="APLICADO"
 * En ACEPTADA: kindCode="PROPUESTA", statusCode="PENDIENTE" | "APLICADO" | "RECHAZADO"
 */
data class ChangeDto(
    @Json(name = "changeId")
    val changeId: Int,
    
    @Json(name = "quoteId")
    val quoteId: Int?,  // ⚠️ Nullable: presente en mensajes de chat (info.change)
    
    @Json(name = "kindCode")
    val kindCode: String,  // "LIBRE" (en TRATO)
    
    @Json(name = "statusCode")
    val statusCode: String,  // "APLICADO" (en TRATO)
    
    @Json(name = "createdBy")
    val createdBy: Int,
    
    @Json(name = "createdAt")
    val createdAt: String,  // ISO 8601
    
    @Json(name = "items")
    val items: List<ChangeItemDto>
)

