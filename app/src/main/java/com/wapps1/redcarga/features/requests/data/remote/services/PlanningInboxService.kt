package com.wapps1.redcarga.features.requests.data.remote.services

import com.wapps1.redcarga.features.requests.data.remote.models.IncomingRequestSummaryDto
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface PlanningInboxService {
    @Headers("X-App-Auth: true")
    @GET("/planning/companies/{companyId}/request-inbox")
    suspend fun getRequestInbox(
        @Path("companyId") companyId: Long,
        @Query("status") status: String? = null  // ✅ Parámetro opcional para filtrar por estado (ej: "OPEN", "CLOSED")
    ): List<IncomingRequestSummaryDto>
}

