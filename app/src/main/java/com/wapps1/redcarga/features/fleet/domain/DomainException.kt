package com.wapps1.redcarga.features.fleet.domain

/**
 * Excepción usada por la capa data para propagar un [DomainError]
 * conservando la causa original para depuración.
 */
class DomainException(
    val reason: DomainError,
    cause: Throwable? = null
) : RuntimeException(reason.message, cause)

