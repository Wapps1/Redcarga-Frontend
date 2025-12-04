package com.wapps1.redcarga.features.chat.domain.models

import java.time.Instant

/**
 * Resumen de un chat (cotización con último mensaje)
 */
data class ChatSummary(
    val quoteId: Long,
    val requestId: Long,
    val companyId: Long,
    val totalAmount: Double,
    val currencyCode: String,
    val createdAt: Instant,
    val lastMessage: ChatMessage?,  // null si no hay mensajes
    val unreadCount: Int = 0       // Por ahora siempre 0, se calculará después
) {
    /**
     * Retorna true si hay mensajes no leídos
     */
    fun hasUnreadMessages(): Boolean = unreadCount > 0

    /**
     * Retorna el texto del último mensaje o un placeholder
     */
    fun getLastMessagePreview(): String {
        return when {
            lastMessage == null -> "Sin mensajes"
            lastMessage.isImageMessage() -> {
                lastMessage.body?.takeIf { it.isNotBlank() } 
                    ?: "📷 Imagen"
            }
            else -> lastMessage.body?.take(50) ?: ""
        }
    }

    /**
     * Retorna la fecha del último mensaje o la fecha de creación de la cotización
     */
    fun getLastActivityDate(): Instant {
        return lastMessage?.createdAt ?: createdAt
    }
}

