package com.wapps1.redcarga.features.chat.domain.models

/**
 * Request para marcar mensajes como leídos
 */
data class MarkChatReadRequest(
    val lastSeenMessageId: Long
)

