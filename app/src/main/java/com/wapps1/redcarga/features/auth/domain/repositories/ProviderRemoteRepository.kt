package com.wapps1.redcarga.features.auth.domain.repositories

import com.wapps1.redcarga.features.auth.domain.models.provider.*

/**
 * Repositorio remoto para endpoints del BC Provider
 */
interface ProviderRemoteRepository {
    suspend fun registerCompany(request: CompanyRegisterRequest): CompanyRegisterResult
}
