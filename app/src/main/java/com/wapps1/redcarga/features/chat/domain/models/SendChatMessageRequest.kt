package com.wapps1.redcarga.features.chat.domain.models

import java.util.UUID

/**
 * Request para enviar un mensaje de chat
 */
data class SendChatMessageRequest(
    val dedupKey: UUID?,           // UUID opcional para idempotencia
    val kind: ChatMessageKind,     // Tipo de mensaje (TEXT o IMAGE)
    val text: String?,             // Texto (requerido si kind=TEXT)
    val url: String?,              // URL de imagen (requerido si kind=IMAGE)
    val caption: String?           // Pie de foto (opcional si kind=IMAGE)
) {
    fun isValid(): Boolean {
        return when (kind) {
            ChatMessageKind.TEXT -> {
                text != null && text.isNotBlank() && url == null && caption == null
            }
            ChatMessageKind.IMAGE -> {
                url != null && url.isNotBlank() && text == null
            }
        }
    }
}

