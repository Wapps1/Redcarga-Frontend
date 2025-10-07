package com.wapps1.redcarga.features.auth.data.repositories

import com.wapps1.redcarga.features.auth.data.mappers.toDomain
import com.wapps1.redcarga.features.auth.data.mappers.toDto
import com.wapps1.redcarga.features.auth.data.network.toDomainError
import com.wapps1.redcarga.features.auth.domain.DomainException
import com.wapps1.redcarga.features.auth.data.remote.services.ProviderService
import com.wapps1.redcarga.features.auth.domain.models.provider.*
import com.wapps1.redcarga.features.auth.domain.repositories.ProviderRemoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProviderRemoteRepositoryImpl @Inject constructor(
    private val service: ProviderService
) : ProviderRemoteRepository {

    override suspend fun registerCompany(
        request: CompanyRegisterRequest
    ): CompanyRegisterResult = withContext(Dispatchers.IO) {
        runCatching {
            service.registerCompany(request.toDto()).toDomain()
        }.getOrElse { err -> throw DomainException(err.toDomainError(), err) }
    }
}
