package com.wapps1.redcarga.features.requests.data.repositories

import com.wapps1.redcarga.core.session.AuthSessionStore
import com.wapps1.redcarga.core.session.SessionState
import com.wapps1.redcarga.features.requests.data.mappers.RequestsMappers.toDto
import com.wapps1.redcarga.features.requests.data.mappers.RequestsMappers.toDomain
import com.wapps1.redcarga.features.requests.data.mappers.RequestsMappers.toDomainSummary
import com.wapps1.redcarga.features.requests.data.mappers.RequestsMappers.toEntity
import com.wapps1.redcarga.features.requests.data.remote.services.RequestsService
import com.wapps1.redcarga.features.requests.domain.RequestsDomainError
import com.wapps1.redcarga.features.requests.domain.models.*
import com.wapps1.redcarga.features.requests.domain.repositories.RequestsRepository
import com.wapps1.redcarga.features.requests.domain.repositories.RequestsLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class RequestsRepositoryImpl @Inject constructor(
    private val remote: RequestsService,
    private val local: RequestsLocalRepository,
    private val authSessionStore: AuthSessionStore
) : RequestsRepository {

    override suspend fun createRequest(request: CreateRequestRequest): CreateRequestResponse {
        return try {
            val dto = request.toDto()
            val response = remote.createRequest(dto)
            CreateRequestResponse(response.requestId)
        } catch (e: Exception) {
            throw RequestsDomainError.NetworkError
        }
    }

    override suspend fun getClientRequests(): List<RequestSummary> {
        return try {
            local.getClientRequests()
        } catch (e: Exception) {
            throw RequestsDomainError.NetworkError
        }
    }

    override fun observeClientRequests(): Flow<List<RequestSummary>> {
        return authSessionStore.sessionState.flatMapLatest { sessionState ->
            when (sessionState) {
                is SessionState.AppSignedIn -> {
                    local.observeClientRequests()
                }
                else -> {
                    flowOf(emptyList())
                }
            }
        }
    }

    override suspend fun getRequestById(requestId: Long): Request {
        return try {
            local.getRequestById(requestId)
                ?: throw RequestsDomainError.RequestNotFound
        } catch (e: RequestsDomainError) {
            throw e
        } catch (e: Exception) {
            throw RequestsDomainError.NetworkError
        }
    }

    override suspend fun refreshClientRequests() {
        try {
            val summaries = remote.getClientRequests()
            local.saveRequestSummaries(summaries.map { it.toDomain() })
        } catch (e: Exception) {
            throw RequestsDomainError.NetworkError
        }
    }

    override suspend fun refreshRequestById(requestId: Long) {
        try {
            val request = remote.getRequestById(requestId)
            local.saveRequest(request.toDomain())
        } catch (e: Exception) {
            throw RequestsDomainError.NetworkError
        }
    }
}