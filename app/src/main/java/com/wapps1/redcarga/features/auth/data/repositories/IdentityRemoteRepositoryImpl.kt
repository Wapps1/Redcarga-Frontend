package com.wapps1.redcarga.features.auth.data.repositories

import com.wapps1.redcarga.features.auth.data.mappers.toDomain
import com.wapps1.redcarga.features.auth.data.mappers.toDto
import com.wapps1.redcarga.features.auth.data.network.toDomainError
import com.wapps1.redcarga.features.auth.domain.DomainException
import com.wapps1.redcarga.features.auth.data.remote.services.IdentityService
import com.wapps1.redcarga.features.auth.domain.models.identity.*
import com.wapps1.redcarga.features.auth.domain.repositories.IdentityRemoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IdentityRemoteRepositoryImpl @Inject constructor(
    private val service: IdentityService
) : IdentityRemoteRepository {

    override suspend fun verifyAndCreatePerson(
        request: PersonCreateRequest
    ): PersonCreateResult = withContext(Dispatchers.IO) {
        runCatching {
            service.verifyAndCreate(request.toDto()).toDomain()
        }.getOrElse { err -> throw DomainException(err.toDomainError(), err) }
    }
}
