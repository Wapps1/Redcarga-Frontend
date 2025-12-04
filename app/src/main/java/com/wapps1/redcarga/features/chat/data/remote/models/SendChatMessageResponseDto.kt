package com.wapps1.redcarga.features.chat.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO de respuesta al enviar un mensaje
 */
@JsonClass(generateAdapter = true)
data class SendChatMessageResponseDto(
    @Json(name = "ok") val ok: Boolean,
    @Json(name = "messageId") val messageId: Int,
    @Json(name = "createdAt") val createdAt: String  // ISO 8601
)

