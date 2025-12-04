package com.wapps1.redcarga.features.chat.domain.models

import com.wapps1.redcarga.features.deals.domain.models.Change
import java.time.Instant
import java.util.UUID

/**
 * Mensaje de chat en una cotización
 */
data class ChatMessage(
    val messageId: Long,
    val quoteId: Long,
    val typeCode: ChatMessageType,
    val contentCode: ChatMessageKind,
    val body: String?,              // Texto del mensaje (null si es IMAGE)
    val mediaUrl: String?,          // URL de la imagen (null si es TEXT)
    val clientDedupKey: UUID?,      // UUID para idempotencia (null si no hay)
    val createdBy: Long,            // accountId del remitente
    val createdAt: Instant,
    val systemSubtypeCode: String?, // null si es USER
    val info: String?,              // null si es USER (JSON string con info adicional)
    val change: Change? = null,     // Cambio aplicado (solo presente si systemSubtypeCode == "CHANGE_APPLIED")
    val acceptanceId: Long? = null  // ID de propuesta de aceptación (solo presente si systemSubtypeCode == "ACCEPTANCE_REQUEST")
) {
    fun isTextMessage(): Boolean = contentCode == ChatMessageKind.TEXT
    fun isImageMessage(): Boolean = contentCode == ChatMessageKind.IMAGE
    fun isUserMessage(): Boolean = typeCode == ChatMessageType.USER
    fun isSystemMessage(): Boolean = typeCode == ChatMessageType.SYSTEM
    fun isChangeAppliedMessage(): Boolean = systemSubtypeCode == "CHANGE_APPLIED"
    fun isChangeProposedMessage(): Boolean = systemSubtypeCode == "CHANGE_PROPOSED"
    fun isChangeAcceptedMessage(): Boolean = systemSubtypeCode == "CHANGE_ACCEPTED"
    fun isChangeRejectedMessage(): Boolean = systemSubtypeCode == "CHANGE_REJECTED"
    fun isAcceptanceRequestMessage(): Boolean = systemSubtypeCode == "ACCEPTANCE_REQUEST"
    fun isAcceptanceConfirmedMessage(): Boolean = systemSubtypeCode == "ACCEPTANCE_CONFIRMED"
    fun isAcceptanceRejectedMessage(): Boolean = systemSubtypeCode == "ACCEPTANCE_REJECTED"
    fun isQuoteRejectedMessage(): Boolean = systemSubtypeCode == "QUOTE_REJECTED"
}

