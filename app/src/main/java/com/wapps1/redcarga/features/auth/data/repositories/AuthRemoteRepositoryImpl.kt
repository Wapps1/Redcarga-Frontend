package com.wapps1.redcarga.features.auth.data.repositories

import com.wapps1.redcarga.features.auth.data.mappers.toDomain
import com.wapps1.redcarga.features.auth.data.mappers.toDto
import com.wapps1.redcarga.features.auth.data.mappers.toDomainSession
import com.wapps1.redcarga.features.auth.data.mappers.toAccountSnapshotDomain
import com.wapps1.redcarga.features.auth.data.network.toDomainError
import com.wapps1.redcarga.features.auth.domain.DomainException
import com.wapps1.redcarga.features.auth.data.remote.services.AuthService
import com.wapps1.redcarga.features.auth.domain.models.iam.*
import com.wapps1.redcarga.features.auth.domain.models.session.*
import com.wapps1.redcarga.features.auth.domain.repositories.AuthRemoteRepository
import com.wapps1.redcarga.features.auth.domain.repositories.SecureTokenRepository
import com.wapps1.redcarga.features.auth.domain.repositories.AuthLocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRemoteRepositoryImpl @Inject constructor(
    private val service: AuthService,
    private val secureTokenRepository: SecureTokenRepository,
    private val authLocalRepository: AuthLocalRepository
) : AuthRemoteRepository {

    override suspend fun registerStart(request: RegistrationRequest): RegistrationStartResult =
        withContext(Dispatchers.IO) {
            runCatching {
                service.registerStart(request.toDto()).toDomain()
            }.getOrElse { err -> throw DomainException(err.toDomainError(), err) }
        }

    override suspend fun login(request: AppLoginRequest): AppSession =
        withContext(Dispatchers.IO) {
            runCatching {
                val dto = service.login(request.toDto())
                val now = System.currentTimeMillis()

                val session = dto.toDomainSession(now)
                val snapshot = dto.toAccountSnapshotDomain()

                // persistir en un paso
                secureTokenRepository.saveAppSession(session)
                if (snapshot != null) authLocalRepository.saveAccountSnapshot(snapshot)

                session
            }.getOrElse { err -> throw DomainException(err.toDomainError(), err) }
        }
}
