package com.wapps1.redcarga.features.fleet.data.remote.services

import com.wapps1.redcarga.features.fleet.data.remote.models.DriverCompanyAssociationRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Servicio para endpoints del módulo Providers
 * Usado para asociar operadores (conductores) a compañías
 */
interface ProvidersService {
    /**
     * Paso 3: Asociar conductor a compañía con rol DRIVER
     * POST /providers/company/{companyId}/operators
     */
    @Headers("X-App-Auth: true")
    @POST("providers/company/{companyId}/operators")
    suspend fun associateOperatorToCompany(
        @Path("companyId") companyId: Long,
        @Body body: DriverCompanyAssociationRequestDto
    ): Response<Unit>
}

