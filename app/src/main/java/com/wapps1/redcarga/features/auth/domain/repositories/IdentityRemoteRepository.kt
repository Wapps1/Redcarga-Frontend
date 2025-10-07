package com.wapps1.redcarga.features.auth.domain.repositories

import com.wapps1.redcarga.features.auth.domain.models.identity.*

/**
 * Repositorio remoto para endpoints del BC Identity
 */
interface IdentityRemoteRepository {
    suspend fun verifyAndCreatePerson(request: PersonCreateRequest): PersonCreateResult
}
