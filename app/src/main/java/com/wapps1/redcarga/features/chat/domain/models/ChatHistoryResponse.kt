package com.wapps1.redcarga.features.chat.domain.models

/**
 * Respuesta del historial completo de un chat
 * Incluye el último mensaje leído y todos los mensajes ordenados por messageId ascendente
 */
data class ChatHistoryResponse(
    val lastReadMessageId: Long?,  // null si nunca ha leído
    val messages: List<ChatMessage>
)

