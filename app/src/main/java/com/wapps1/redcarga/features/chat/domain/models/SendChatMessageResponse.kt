package com.wapps1.redcarga.features.chat.domain.models

import java.time.Instant

/**
 * Response al enviar un mensaje
 */
data class SendChatMessageResponse(
    val ok: Boolean,
    val messageId: Long,
    val createdAt: Instant
)

