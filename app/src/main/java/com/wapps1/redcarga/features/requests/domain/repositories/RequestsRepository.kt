package com.wapps1.redcarga.features.requests.domain.repositories

import com.wapps1.redcarga.features.requests.domain.models.*
import kotlinx.coroutines.flow.Flow

interface RequestsRepository {
    suspend fun createRequest(request: CreateRequestRequest): CreateRequestResponse
    
    suspend fun getClientRequests(): List<RequestSummary>
    fun observeClientRequests(): Flow<List<RequestSummary>>
    
    suspend fun getRequestById(requestId: Long): Request
    
    suspend fun refreshClientRequests()
    suspend fun refreshRequestById(requestId: Long)
}
