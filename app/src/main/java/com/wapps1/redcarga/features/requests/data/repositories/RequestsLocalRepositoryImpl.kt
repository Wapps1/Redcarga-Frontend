package com.wapps1.redcarga.features.requests.data.repositories

import com.wapps1.redcarga.core.session.AuthSessionStore
import com.wapps1.redcarga.core.session.SessionState
import com.wapps1.redcarga.features.requests.data.local.dao.RequestsDao
import com.wapps1.redcarga.features.requests.data.local.dao.RequestsDao.RequestWithItems
import com.wapps1.redcarga.features.requests.data.mappers.RequestsMappers.toDomain
import com.wapps1.redcarga.features.requests.data.mappers.RequestsMappers.toDomainSummary
import com.wapps1.redcarga.features.requests.data.mappers.RequestsMappers.toEntity
import com.wapps1.redcarga.features.requests.domain.models.*
import com.wapps1.redcarga.features.requests.domain.repositories.RequestsLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestsLocalRepositoryImpl @Inject constructor(
    private val dao: RequestsDao,
    private val authSessionStore: AuthSessionStore
) : RequestsLocalRepository {

    override suspend fun saveRequest(request: Request) {
        val entity = request.toEntity()
        val itemEntities = request.items.map { it.toEntity(request.requestId) }
        val imageEntities = request.items.flatMap { item ->
            item.images.map { image -> image.toEntity(item.itemId ?: 0L) }
        }
        dao.saveRequestWithItems(entity, itemEntities, imageEntities)
    }

    override suspend fun saveRequestSummary(summary: RequestSummary) {
        val entity = summary.toEntity()
        val accountId = getCurrentAccountId()
        entity.copy(requesterAccountId = accountId).let { updatedEntity ->
            dao.upsertRequest(updatedEntity)
        }
    }

    override suspend fun saveRequests(requests: List<Request>) {
        requests.forEach { request ->
            saveRequest(request)
        }
    }

    override suspend fun saveRequestSummaries(summaries: List<RequestSummary>) {
        val accountId = getCurrentAccountId()
        summaries.forEach { summary ->
            val entity = summary.toEntity().copy(requesterAccountId = accountId)
            dao.upsertRequest(entity)
        }
    }

    override suspend fun getRequestById(requestId: Long): Request? {
        return dao.getRequest(requestId)?.toDomain()
    }

    override suspend fun getClientRequests(): List<RequestSummary> {
        val accountId = getCurrentAccountId()
        return dao.getClientRequests(accountId).map { it.toDomainSummary() }
    }

    override fun observeClientRequests(): Flow<List<RequestSummary>> {
        return authSessionStore.sessionState.flatMapLatest { sessionState ->
            when (sessionState) {
                is SessionState.AppSignedIn -> {
                    dao.observeClientRequests(sessionState.app.accountId).map { list ->
                        list.map { it.toDomainSummary() }
                    }
                }
                else -> {
                    flowOf(emptyList())
                }
            }
        }
    }

    override fun observeRequestById(requestId: Long): Flow<Request?> {
        return dao.observeRequest(requestId).map { requestWithItems: RequestWithItems? ->
            requestWithItems?.toDomain()
        }
    }

    override suspend fun deleteRequest(requestId: Long) {
        dao.deleteRequest(requestId)
    }

    override suspend fun deleteAllRequests() {
        val accountId = getCurrentAccountId()
        dao.clearForAccount(accountId)
        dao.clearItemsForAccount(accountId)
        dao.clearImagesForAccount(accountId)
    }

    override suspend fun hasRequest(requestId: Long): Boolean {
        return dao.hasRequest(requestId)
    }

    override suspend fun hasClientRequests(): Boolean {
        val accountId = getCurrentAccountId()
        return dao.hasClientRequests(accountId)
    }

    private suspend fun getCurrentAccountId(): Long {
        return when (val sessionState = authSessionStore.sessionState.value) {
            is SessionState.AppSignedIn -> sessionState.app.accountId
            else -> throw IllegalStateException("No authenticated user")
        }
    }
}
