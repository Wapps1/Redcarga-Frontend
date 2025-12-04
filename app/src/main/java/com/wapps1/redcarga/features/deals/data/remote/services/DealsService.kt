package com.wapps1.redcarga.features.deals.data.remote.services

import com.wapps1.redcarga.features.deals.data.remote.models.*
import retrofit2.http.*

/**
 * Servicio para endpoints de cambios y aceptaciones en cotizaciones
 */
interface DealsService {
    /**
     * Aplica cambios a una cotización
     * POST /api/deals/quotes/{quoteId}/changes
     * 
     * En TRATO:
     * - Los cambios se aplican inmediatamente
     * - Se crea un Change con kind="LIBRE" y status="APLICADO"
     * - Se genera un mensaje CHANGE_APPLIED en el chat
     * 
     * En ACEPTADA:
     * - Crea una propuesta de cambio (no aplica cambios aún)
     * - Se crea un Change con kind="PROPUESTA" y status="PENDIENTE"
     * - Se genera un mensaje CHANGE_PROPOSED en el chat
     * 
     * @param quoteId ID de la cotización
     * @param request Datos de los cambios a aplicar
     * @param ifMatch Versión de la cotización para control de concurrencia (opcional)
     * @param idempotencyKey Clave para evitar duplicados (opcional)
     * @return Response con el changeId creado
     */
    @Headers("X-App-Auth: true")
    @POST("/api/deals/quotes/{quoteId}/changes")
    suspend fun applyChange(
        @Path("quoteId") quoteId: Long,
        @Body request: ApplyChangeRequestDto,
        @Header("If-Match") ifMatch: String? = null,
        @Header("Idempotency-Key") idempotencyKey: String? = null
    ): ApplyChangeResponseDto

    /**
     * Propone aceptación de una cotización
     * POST /api/deals/quotes/{quoteId}/acceptances
     * 
     * Estados permitidos: TRATO o EN_ESPERA (no ACEPTADA)
     * Genera mensaje ACCEPTANCE_REQUEST en el chat
     * 
     * @param quoteId ID de la cotización
     * @param request Datos de la propuesta (idempotencyKey, note)
     * @return Response con el acceptanceId creado
     */
    @Headers("X-App-Auth: true")
    @POST("/api/deals/quotes/{quoteId}/acceptances")
    suspend fun proposeAcceptance(
        @Path("quoteId") quoteId: Long,
        @Body request: AcceptanceRequestDto
    ): AcceptanceResponseDto

    /**
     * Confirma una propuesta de aceptación
     * POST /api/deals/quotes/{quoteId}/acceptances/{acceptanceId}/confirm
     * 
     * Cambia la cotización a estado ACEPTADA
     * Genera mensaje ACCEPTANCE_CONFIRMED en el chat
     * 
     * @param quoteId ID de la cotización
     * @param acceptanceId ID de la propuesta a confirmar
     * @return Response con ok: true
     */
    @Headers("X-App-Auth: true")
    @POST("/api/deals/quotes/{quoteId}/acceptances/{acceptanceId}/confirm")
    suspend fun confirmAcceptance(
        @Path("quoteId") quoteId: Long,
        @Path("acceptanceId") acceptanceId: Long
    ): ConfirmAcceptanceResponseDto

    /**
     * Rechaza una propuesta de aceptación
     * POST /api/deals/quotes/{quoteId}/acceptances/{acceptanceId}/reject
     * 
     * La cotización NO cambia de estado (sigue en TRATO o EN_ESPERA)
     * Genera mensaje ACCEPTANCE_REJECTED en el chat
     * 
     * @param quoteId ID de la cotización
     * @param acceptanceId ID de la propuesta a rechazar
     * @return Response con ok: true
     */
    @Headers("X-App-Auth: true")
    @POST("/api/deals/quotes/{quoteId}/acceptances/{acceptanceId}/reject")
    suspend fun rejectAcceptance(
        @Path("quoteId") quoteId: Long,
        @Path("acceptanceId") acceptanceId: Long
    ): ConfirmAcceptanceResponseDto

    /**
     * Decide sobre un cambio propuesto (aceptar o rechazar)
     * POST /api/deals/quotes/{quoteId}/changes/{changeId}/decision
     * 
     * Solo disponible en estado ACEPTADA
     * El Change debe estar en estado PENDIENTE
     * El usuario no puede decidir sobre su propio cambio
     * 
     * Si accept=true:
     * - Aplica los cambios a la cotización
     * - Marca el Change como APLICADO
     * - Genera mensaje CHANGE_ACCEPTED en el chat
     * 
     * Si accept=false:
     * - Marca el Change como RECHAZADO
     * - No aplica cambios
     * - Genera mensaje CHANGE_REJECTED en el chat
     * 
     * @param quoteId ID de la cotización
     * @param changeId ID del cambio propuesto
     * @param request Datos de la decisión (accept: true/false)
     * @param ifMatch Versión de la cotización para control de concurrencia (opcional)
     * @return Response vacío (HTTP 204 No Content)
     */
    @Headers("X-App-Auth: true")
    @POST("/api/deals/quotes/{quoteId}/changes/{changeId}/decision")
    suspend fun decisionChange(
        @Path("quoteId") quoteId: Long,
        @Path("changeId") changeId: Long,
        @Body request: ChangeDecisionRequestDto,
        @Header("If-Match") ifMatch: String? = null
    ): retrofit2.Response<Unit>

    /**
     * Obtiene el detalle de un cambio
     * GET /api/deals/quotes/{quoteId}/changes/{changeId}
     * 
     * @param quoteId ID de la cotización
     * @param changeId ID del cambio
     * @return DTO con el detalle completo del cambio
     */
    @Headers("X-App-Auth: true")
    @GET("/api/deals/quotes/{quoteId}/changes/{changeId}")
    suspend fun getChangeDetail(
        @Path("quoteId") quoteId: Long,
        @Path("changeId") changeId: Long
    ): ChangeDto
}

