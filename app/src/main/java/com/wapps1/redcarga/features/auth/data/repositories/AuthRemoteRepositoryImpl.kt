package com.wapps1.redcarga.features.auth.data.repositories

import android.util.Log
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

private const val TAG = "AuthRemoteRepo"

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
                Log.d(TAG, "🌐 Llamando POST /iam/login con platform=${request.platform}")

                val dto = service.login(request.toDto())

                Log.d(TAG, "✅ Backend respondió:")
                Log.d(TAG, "   sessionId: ${dto.sessionId}")
                Log.d(TAG, "   accountId: ${dto.accountId}")
                Log.d(TAG, "   roles: ${dto.roles}")
                Log.d(TAG, "   account: ${if (dto.account != null) "EXISTE" else "NULL"}")

                if (dto.account != null) {
                    Log.d(TAG, "   account.username: ${dto.account.username}")
                    Log.d(TAG, "   account.email: ${dto.account.email}")
                    Log.d(TAG, "   account.companyId: ${dto.account.companyId}")
                } else {
                    Log.e(TAG, "   ❌ account es NULL - NO HAY COMPANYID")
                }

                val now = System.currentTimeMillis()

                val session = dto.toDomainSession(now)
                val snapshot = dto.toAccountSnapshotDomain()

                Log.d(TAG, "📦 Sesión mapeada:")
                Log.d(TAG, "   companyId: ${session.companyId}")
                Log.d(TAG, "   roles: ${session.roles}")
                Log.d(TAG, "   userType: ${session.roles.firstOrNull()}")

                // persistir en un paso
                secureTokenRepository.saveAppSession(session)
                if (snapshot != null) authLocalRepository.saveAccountSnapshot(snapshot)

                Log.d(TAG, "✅ Sesión guardada en SecureTokenRepository")

                session
            }.getOrElse { err ->
                Log.e(TAG, "❌ Error en login", err)
                throw DomainException(err.toDomainError(), err)
            }
        }
}
