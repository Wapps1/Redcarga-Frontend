package com.wapps1.redcarga.features.auth.domain

/**
 * Excepción checked por la capa data para propagar un [DomainError]
 * conservando la causa original para depuración.
 */
class DomainException(
    val reason: DomainError,
    cause: Throwable? = null
) : RuntimeException(
    when (reason) {
        is DomainError.InvalidData -> reason.reason
        is DomainError.Http -> reason.message ?: "Error ${reason.code}"
        DomainError.Network -> "Sin conexión"
        DomainError.Unauthorized -> "No autorizado"
        DomainError.Forbidden -> "Prohibido"
        DomainError.EmailNotVerified -> "Email no verificado"
        DomainError.Unknown -> cause?.message ?: "Error desconocido"
    },
    cause
)


