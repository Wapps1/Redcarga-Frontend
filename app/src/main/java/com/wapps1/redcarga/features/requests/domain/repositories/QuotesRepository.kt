package com.wapps1.redcarga.features.requests.domain.repositories

import com.wapps1.redcarga.features.requests.domain.models.CreateQuoteRequest
import com.wapps1.redcarga.features.requests.domain.models.CreateQuoteResponse
import com.wapps1.redcarga.features.requests.domain.models.QuoteDetail
import com.wapps1.redcarga.features.requests.domain.models.QuoteSummary
import kotlinx.coroutines.flow.Flow

/**
 * Repository para gestionar cotizaciones
 */
interface QuotesRepository {

    /**
     * Crea una nueva cotización para una solicitud
     */
    suspend fun createQuote(request: CreateQuoteRequest): CreateQuoteResponse

    /**
     * Obtiene la lista de cotizaciones de una compañía (con caché)
     */
    fun observeQuotesByCompany(companyId: Long): Flow<List<QuoteSummary>>

    /**
     * Refresca las cotizaciones desde el backend
     */
    suspend fun refreshQuotesByCompany(companyId: Long)

    /**
     * Obtiene el detalle de una cotización específica
     */
    suspend fun getQuoteDetail(quoteId: Long): QuoteDetail

    /**
     * Obtiene las cotizaciones de una solicitud específica con sus detalles completos
     * GET /api/deals/quotes?requestId={requestId}&state={state}
     * @param state Opcional: PENDING, TRATO, RECHAZADA. Si es null, retorna todas las cotizaciones.
     * Luego obtiene el detalle de cada cotización
     */
    suspend fun getQuotesByRequestId(requestId: Long, state: String? = null): List<QuoteDetail>

    /**
     * Inicia la negociación de una cotización
     */
    suspend fun startNegotiation(quoteId: Long): Result<Unit>

    /**
     * Rechaza una cotización
     */
    suspend fun rejectQuote(quoteId: Long): Result<Unit>
}

