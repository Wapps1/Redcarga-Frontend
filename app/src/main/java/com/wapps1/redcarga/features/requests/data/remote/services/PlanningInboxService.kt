package com.wapps1.redcarga.features.requests.data.remote.services

import com.wapps1.redcarga.features.requests.data.remote.models.IncomingRequestSummaryDto
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

/**
 * Servicio Retrofit para el inbox de solicitudes entrantes de proveedores
 */
interface PlanningInboxService {
    
    /**
     * Obtiene todas las solicitudes entrantes para una compañía específica
     * Endpoint: GET /planning/companies/{companyId}/request-inbox
     * Requiere: Token IAM (X-App-Auth: true)
     */
    @Headers("X-App-Auth: true")
    @GET("/planning/companies/{companyId}/request-inbox")
    suspend fun getRequestInbox(
        @Path("companyId") companyId: Long
    ): List<IncomingRequestSummaryDto>
}

