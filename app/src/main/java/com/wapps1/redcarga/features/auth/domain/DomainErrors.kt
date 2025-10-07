package com.wapps1.redcarga.features.auth.domain

/**
 * Errores de dominio normalizados
 * La capa data mapeará Retrofit/HTTP/Firebase exceptions → DomainError
 */
sealed interface DomainError {
    data object Network : DomainError
    data class Http(val code: Int, val message: String?) : DomainError
    data object Unauthorized : DomainError                 // idToken inválido/expirado
    data object Forbidden : DomainError
    data class InvalidData(val reason: String) : DomainError
    data object EmailNotVerified : DomainError             // si backend lo emite
    data object Unknown : DomainError
}
