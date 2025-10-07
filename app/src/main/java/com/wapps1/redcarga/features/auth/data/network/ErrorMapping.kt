package com.wapps1.redcarga.features.auth.data.network

import com.wapps1.redcarga.features.auth.domain.DomainError
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException

fun Throwable.toDomainError(): DomainError = when (this) {
    is IOException -> DomainError.Network
    is HttpException -> {
        val code = code()
        val body = response()?.errorBody()?.string()
        when (code) {
            401 -> DomainError.Unauthorized
            403 -> DomainError.Forbidden
            // Ajusta esta condición si tu backend usa otro código/mensaje
            409 -> DomainError.EmailNotVerified
            in 400..499 -> DomainError.InvalidData(body ?: message())
            in 500..599 -> DomainError.Http(code, body ?: message())
            else -> DomainError.Unknown
        }
    }
    else -> DomainError.Unknown
}
