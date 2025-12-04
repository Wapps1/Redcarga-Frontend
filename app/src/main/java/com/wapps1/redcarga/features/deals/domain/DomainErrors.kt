package com.wapps1.redcarga.features.deals.domain

/**
 * Errores de dominio para el módulo de deals
 */
sealed class DealsDomainError : Exception() {
    object NetworkError : DealsDomainError()
    object InvalidChangeData : DealsDomainError()
    object NotChatParticipant : DealsDomainError()  // No es participante del chat
    object QuoteNotFound : DealsDomainError()
    object Unauthorized : DealsDomainError()
    object ServerError : DealsDomainError()
    object VersionConflict : DealsDomainError()  // If-Match conflict

    data class ValidationError(val field: String, override val message: String) : DealsDomainError()
    data class UnknownError(override val message: String) : DealsDomainError()
}

