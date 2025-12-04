package com.wapps1.redcarga.features.deals.domain.repositories

import com.wapps1.redcarga.features.deals.domain.models.*

/**
 * Repositorio para operaciones de cambios y aceptaciones en cotizaciones
 */
interface DealsRepository {
    /**
     * Aplica cambios a una cotización
     * En TRATO: Los cambios se aplican inmediatamente (kind=LIBRE, status=APLICADO), genera mensaje CHANGE_APPLIED
     * En ACEPTADA: Crea una propuesta de cambio (kind=PROPUESTA, status=PENDIENTE), genera mensaje CHANGE_PROPOSED
     * 
     * @param quoteId ID de la cotización
     * @param request Datos de los cambios a aplicar
     * @param ifMatch Versión de la cotización para control de concurrencia (opcional)
     * @param idempotencyKey Clave para evitar duplicados (opcional)
     * @return Response con el changeId creado
     */
    suspend fun applyChange(
        quoteId: Long,
        request: ApplyChangeRequest,
        ifMatch: String? = null,
        idempotencyKey: String? = null
    ): ApplyChangeResponse

    /**
     * Propone aceptación de una cotización
     * Estados permitidos: TRATO o EN_ESPERA (no ACEPTADA)
     * Genera mensaje ACCEPTANCE_REQUEST en el chat
     * 
     * @param quoteId ID de la cotización
     * @param request Datos de la propuesta (idempotencyKey, note)
     * @return Response con el acceptanceId creado
     */
    suspend fun proposeAcceptance(
        quoteId: Long,
        request: AcceptanceRequest
    ): AcceptanceResponse

    /**
     * Confirma una propuesta de aceptación
     * Cambia la cotización a estado ACEPTADA
     * Genera mensaje ACCEPTANCE_CONFIRMED en el chat
     * 
     * @param quoteId ID de la cotización
     * @param acceptanceId ID de la propuesta a confirmar
     * @return Response con ok: true
     */
    suspend fun confirmAcceptance(
        quoteId: Long,
        acceptanceId: Long
    ): ConfirmAcceptanceResponse

    /**
     * Rechaza una propuesta de aceptación
     * La cotización NO cambia de estado (sigue en TRATO o EN_ESPERA)
     * Genera mensaje ACCEPTANCE_REJECTED en el chat
     * 
     * @param quoteId ID de la cotización
     * @param acceptanceId ID de la propuesta a rechazar
     * @return Response con ok: true
     */
    suspend fun rejectAcceptance(
        quoteId: Long,
        acceptanceId: Long
    ): ConfirmAcceptanceResponse

    /**
     * Decide sobre un cambio propuesto (aceptar o rechazar)
     * Solo disponible en estado ACEPTADA
     * El Change debe estar en estado PENDIENTE
     * 
     * Si accept=true: Aplica cambios, marca Change como APLICADO, genera mensaje CHANGE_ACCEPTED
     * Si accept=false: Marca Change como RECHAZADO, genera mensaje CHANGE_REJECTED
     * 
     * @param quoteId ID de la cotización
     * @param changeId ID del cambio propuesto
     * @param accept true para aceptar, false para rechazar
     * @param ifMatch Versión de la cotización para control de concurrencia (opcional)
     */
    suspend fun decisionChange(
        quoteId: Long,
        changeId: Long,
        accept: Boolean,
        ifMatch: String? = null
    ): Unit

    /**
     * Obtiene el detalle de un cambio
     * 
     * @param quoteId ID de la cotización
     * @param changeId ID del cambio
     * @return Modelo de dominio con el detalle completo del cambio
     */
    suspend fun getChangeDetail(
        quoteId: Long,
        changeId: Long
    ): Change
}

