package com.wapps1.redcarga.features.auth.data.network

import com.wapps1.redcarga.features.auth.domain.repositories.SecureTokenRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class FirebaseIdTokenInterceptor @Inject constructor(
    private val secureTokenRepository: SecureTokenRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val needsFirebase = req.header("X-Firebase-Auth") == "true"

        val newReq = if (needsFirebase) {
            val fb = runBlocking { secureTokenRepository.getFirebaseSession() }
                ?: throw IOException("Missing Firebase token")
            req.newBuilder()
                .removeHeader("X-Firebase-Auth")
                .header("Authorization", "Bearer ${fb.idToken}")
                .build()
        } else req

        return chain.proceed(newReq)
    }
}
