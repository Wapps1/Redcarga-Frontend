package com.wapps1.redcarga.features.auth.domain.repositories

import com.wapps1.redcarga.features.auth.domain.models.iam.*
import com.wapps1.redcarga.features.auth.domain.models.session.*

/**
 * Repositorio remoto para endpoints del BC IAM
 */
interface AuthRemoteRepository {
    suspend fun registerStart(request: RegistrationRequest): RegistrationStartResult
    suspend fun login(request: AppLoginRequest): AppSession
}
