package com.wapps1.redcarga.features.auth.data.repositories

import com.wapps1.redcarga.features.auth.data.mappers.toDomain
import com.wapps1.redcarga.features.auth.data.network.toDomainError
import com.wapps1.redcarga.features.auth.domain.DomainException
import com.wapps1.redcarga.features.auth.data.remote.models.FirebaseSignInRequestDto
import com.wapps1.redcarga.features.auth.data.remote.services.FirebaseAuthService
import com.wapps1.redcarga.features.auth.domain.models.value.Email
import com.wapps1.redcarga.features.auth.domain.models.value.Password
import com.wapps1.redcarga.features.auth.domain.models.firebase.FirebaseSession
import com.wapps1.redcarga.features.auth.domain.repositories.FirebaseAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class FirebaseAuthRepositoryImpl @Inject constructor(
    private val service: FirebaseAuthService,
    @Named("firebaseApiKey") private val apiKey: String
) : FirebaseAuthRepository {

    override suspend fun signInWithPassword(email: Email, password: String): FirebaseSession =
        withContext(Dispatchers.IO) {
            runCatching {
                val body = FirebaseSignInRequestDto(email.value, password, true)  // ✅ password es String directo
                service.signInWithPassword(body, apiKey)
                    .toDomain(System.currentTimeMillis())
            }.getOrElse { err ->
                throw DomainException(err.toDomainError(), err)
            }
        }
}
