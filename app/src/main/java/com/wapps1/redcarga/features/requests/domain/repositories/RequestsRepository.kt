package com.wapps1.redcarga.features.requests.domain.repositories

import com.wapps1.redcarga.features.requests.domain.models.*
import kotlinx.coroutines.flow.Flow

interface RequestsRepository {
    // Crear solicitud (solo clientes)
    suspend fun createRequest(request: CreateRequestRequest): CreateRequestResponse
    
    // Obtener solicitudes del cliente (mis solicitudes)
    suspend fun getClientRequests(): List<RequestSummary>
    fun observeClientRequests(): Flow<List<RequestSummary>>
    
    // Obtener detalle completo de solicitud (clientes y proveedores)
    suspend fun getRequestById(requestId: Long): Request
    
    // Refresh data
    suspend fun refreshClientRequests()
    suspend fun refreshRequestById(requestId: Long)
}
