package com.wapps1.redcarga.features.auth.data.network

import com.wapps1.redcarga.features.auth.domain.repositories.SecureTokenRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class AppAccessTokenInterceptor @Inject constructor(
    private val secureTokenRepository: SecureTokenRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val needsAppAuth = req.header("X-App-Auth") == "true"

        val newReq = if (needsAppAuth) {
            val app = runBlocking { secureTokenRepository.getAppSession() }
                ?: throw IOException("Missing App access token")
            req.newBuilder()
                .removeHeader("X-App-Auth")
                .header("Authorization", "Bearer ${app.accessToken}")
                .build()
        } else req

        return chain.proceed(newReq)
    }
}
