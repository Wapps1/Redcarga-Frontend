package com.wapps1.redcarga.features.requests.domain.repositories

import com.wapps1.redcarga.features.requests.domain.models.IncomingRequestSummary
import com.wapps1.redcarga.features.requests.domain.models.Request
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para gestionar el inbox de solicitudes entrantes para proveedores
 */
interface PlanningInboxRepository {
    
    /**
     * Observa las solicitudes entrantes para una compañía específica
     * Retorna un Flow que emite cada vez que hay cambios en la BD local
     */
    fun observeIncomingRequests(companyId: Long): Flow<List<IncomingRequestSummary>>
    
    /**
     * Refresca las solicitudes entrantes desde el backend
     * Hace GET /planning/companies/{companyId}/request-inbox
     * Actualiza la BD local con los resultados
     */
    suspend fun refreshIncomingRequests(companyId: Long)
    
    /**
     * Obtiene los detalles completos de una solicitud específica
     * Hace GET /requests/{requestId}
     * Retorna el objeto Request completo con todos los detalles
     */
    suspend fun getRequestDetail(requestId: Long): Request
}

