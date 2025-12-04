package com.wapps1.redcarga.features.chat.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO de respuesta del endpoint GET /api/deals/quotes/{quoteId}/chat
 * Retorna el historial completo del chat con el último mensaje leído
 */
@JsonClass(generateAdapter = true)
data class ChatHistoryResponseDto(
    @Json(name = "lastReadMessageId") val lastReadMessageId: Int?,
    @Json(name = "messages") val messages: List<ChatMessageDto>
)

