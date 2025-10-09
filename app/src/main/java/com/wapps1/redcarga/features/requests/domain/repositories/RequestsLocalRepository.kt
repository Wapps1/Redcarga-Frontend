package com.wapps1.redcarga.features.requests.domain.repositories

import com.wapps1.redcarga.features.requests.domain.models.*
import kotlinx.coroutines.flow.Flow

interface RequestsLocalRepository {
    // Guardar solicitudes
    suspend fun saveRequest(request: Request)
    suspend fun saveRequestSummary(summary: RequestSummary)
    suspend fun saveRequests(requests: List<Request>)
    suspend fun saveRequestSummaries(summaries: List<RequestSummary>)
    
    // Obtener solicitudes
    suspend fun getRequestById(requestId: Long): Request?
    suspend fun getClientRequests(): List<RequestSummary>
    fun observeClientRequests(): Flow<List<RequestSummary>>
    fun observeRequestById(requestId: Long): Flow<Request?>
    
    // Eliminar solicitudes
    suspend fun deleteRequest(requestId: Long)
    suspend fun deleteAllRequests()
    
    // Verificar existencia
    suspend fun hasRequest(requestId: Long): Boolean
    suspend fun hasClientRequests(): Boolean
}
