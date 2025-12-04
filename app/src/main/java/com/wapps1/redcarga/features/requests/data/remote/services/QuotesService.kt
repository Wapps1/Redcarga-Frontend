package com.wapps1.redcarga.features.requests.data.remote.services

import com.wapps1.redcarga.features.requests.data.remote.models.CreateQuoteRequestDto
import com.wapps1.redcarga.features.requests.data.remote.models.CreateQuoteResponseDto
import com.wapps1.redcarga.features.requests.data.remote.models.QuoteDetailDto
import com.wapps1.redcarga.features.requests.data.remote.models.QuoteSummaryDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Servicio para endpoints de cotizaciones
 */
interface QuotesService {

    /**
     * Crea una nueva cotización
     * POST /api/deals/quotes
     */
    @Headers("X-App-Auth: true")
    @POST("/api/deals/quotes")
    suspend fun createQuote(
        @Body request: CreateQuoteRequestDto
    ): CreateQuoteResponseDto

    /**
     * Obtiene la lista de cotizaciones de una compañía
     * GET /api/deals/quotes/general?company_id={companyId}&state={state}
     * @param state Opcional: PENDING, TRATO, RECHAZADA, ACEPTADA. Si es null, retorna todas.
     * Nota: Si state=TRATO, incluye también EN_ESPERA automáticamente.
     */
    @Headers("X-App-Auth: true")
    @GET("/api/deals/quotes/general")
    suspend fun getQuotesByCompany(
        @Query("company_id") companyId: Long,
        @Query("state") state: String? = null
    ): List<QuoteSummaryDto>

    /**
     * Obtiene el detalle de una cotización específica
     * GET /api/deals/quotes/{quoteId}/detail
     */
    @Headers("X-App-Auth: true")
    @GET("/api/deals/quotes/{quoteId}/detail")
    suspend fun getQuoteDetail(
        @Path("quoteId") quoteId: Long
    ): QuoteDetailDto

    /**
     * Obtiene las cotizaciones de una solicitud específica
     * GET /api/deals/quotes?requestId={requestId}&state={state}
     * @param state Opcional: PENDING, TRATO, RECHAZADA. Si es null, retorna todas las cotizaciones.
     */
    @Headers("X-App-Auth: true")
    @GET("/api/deals/quotes")
    suspend fun getQuotesByRequestId(
        @Query("requestId") requestId: Long,
        @Query("state") state: String? = null
    ): List<QuoteSummaryDto>

    /**
     * Inicia la negociación de una cotización
     * POST /api/deals/quotes/{quoteId}:start-negotiation
     */
    @Headers("X-App-Auth: true")
    @POST("/api/deals/quotes/{quoteId}:start-negotiation")
    suspend fun startNegotiation(
        @Path("quoteId") quoteId: Long,
        @Header("If-Match") ifMatch: String = "0"
    ): Response<Unit>

    /**
     * Rechaza una cotización
     * POST /api/deals/quotes/{quoteId}:reject
     */
    @Headers("X-App-Auth: true")
    @POST("/api/deals/quotes/{quoteId}:reject")
    suspend fun rejectQuote(
        @Path("quoteId") quoteId: Long
    ): Response<Unit>
}

