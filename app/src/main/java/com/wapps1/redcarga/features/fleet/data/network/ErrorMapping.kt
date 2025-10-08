package com.wapps1.redcarga.features.fleet.data.network

import com.wapps1.redcarga.features.fleet.domain.DomainError
import retrofit2.HttpException
import java.io.IOException

fun Throwable.toFleetDomainError(): DomainError = when (this) {
    is IOException -> DomainError.Network
    is HttpException -> {
        val code = code()
        val body = response()?.errorBody()?.string()
        when (code) {
            401 -> DomainError.Unauthorized
            403 -> DomainError.Forbidden
            404 -> DomainError.NotFound
            in 400..499 -> DomainError.InvalidData(body ?: message())
            in 500..599 -> DomainError.Http(code, body ?: message())
            else -> DomainError.Unknown
        }
    }
    else -> DomainError.Unknown
}


