package com.wapps1.redcarga.features.chat.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO de un mensaje de chat
 */
@JsonClass(generateAdapter = true)
data class ChatMessageDto(
    @Json(name = "messageId") val messageId: Int,
    @Json(name = "quoteId") val quoteId: Int,
    @Json(name = "typeCode") val typeCode: String,        // "USER" o "SYSTEM"
    @Json(name = "contentCode") val contentCode: String, // "TEXT" o "IMAGE"
    @Json(name = "body") val body: String?,
    @Json(name = "mediaUrl") val mediaUrl: String?,
    @Json(name = "clientDedupKey") val clientDedupKey: String?,
    @Json(name = "createdBy") val createdBy: Int,
    @Json(name = "createdAt") val createdAt: String,     // ISO 8601
    @Json(name = "systemSubtypeCode") val systemSubtypeCode: String?,
    @Json(name = "info") val info: Any?  // ⚠️ Cambiado a Any? porque el backend envía Object, no String
)

