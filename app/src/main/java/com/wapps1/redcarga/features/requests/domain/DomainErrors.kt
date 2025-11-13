package com.wapps1.redcarga.features.requests.domain

sealed class RequestsDomainError : Exception() {
    object NetworkError : RequestsDomainError()
    object InvalidRequestData : RequestsDomainError()
    object RequestNotFound : RequestsDomainError()
    object Unauthorized : RequestsDomainError()
    object ServerError : RequestsDomainError()

    data class ValidationError(val field: String, override val message: String) : RequestsDomainError()
    data class UnknownError(override val message: String) : RequestsDomainError()
}
