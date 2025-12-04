package com.wapps1.redcarga.features.chat.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para marcar mensajes como leídos
 */
@JsonClass(generateAdapter = true)
data class MarkChatReadRequestDto(
    @Json(name = "lastSeenMessageId") val lastSeenMessageId: Int
)

