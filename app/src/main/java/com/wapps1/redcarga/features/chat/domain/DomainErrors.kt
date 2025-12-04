package com.wapps1.redcarga.features.chat.domain

/**
 * Errores de dominio para el módulo de chat
 */
sealed class ChatDomainError : Exception() {
    object NetworkError : ChatDomainError()
    object InvalidMessageData : ChatDomainError()
    object NotChatParticipant : ChatDomainError()
    object QuoteNotFound : ChatDomainError()
    object Unauthorized : ChatDomainError()
    object ServerError : ChatDomainError()

    data class ValidationError(val field: String, override val message: String) : ChatDomainError()
    data class UnknownError(override val message: String) : ChatDomainError()
}

