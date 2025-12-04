package com.wapps1.redcarga.features.deals.data.repositories

import android.util.Log
import com.wapps1.redcarga.features.deals.data.mappers.DealsMappers.toDomain
import com.wapps1.redcarga.features.deals.data.mappers.DealsMappers.toDto
import com.wapps1.redcarga.features.deals.data.remote.models.ChangeDecisionRequestDto
import com.wapps1.redcarga.features.deals.data.remote.services.DealsService
import com.wapps1.redcarga.features.deals.domain.DealsDomainError
import com.wapps1.redcarga.features.deals.domain.models.*
import com.wapps1.redcarga.features.deals.domain.repositories.DealsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "DealsRepository"

/**
 * Implementación del repositorio de deals
 */
@Singleton
class DealsRepositoryImpl @Inject constructor(
    private val dealsService: DealsService
) : DealsRepository {

    override suspend fun applyChange(
        quoteId: Long,
        request: ApplyChangeRequest,
        ifMatch: String?,
        idempotencyKey: String?
    ): ApplyChangeResponse = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📝 Aplicando cambios a cotización: quoteId=$quoteId")
            Log.d(TAG, "   (En TRATO: se aplican inmediatamente. En ACEPTADA: se crea propuesta)")
            Log.d(TAG, "   items=${request.items.size}, ifMatch=$ifMatch, idempotencyKey=$idempotencyKey")
            
            // Log detallado de items
            request.items.forEachIndexed { index, item ->
                Log.d(TAG, "   Item[$index]: fieldCode=${item.fieldCode}, " +
                        "targetQuoteItemId=${item.targetQuoteItemId}, " +
                        "oldValue=${item.oldValue}, newValue=${item.newValue}")
            }

            // Validar request
            if (!request.isValid()) {
                Log.e(TAG, "❌ Request inválido")
                throw DealsDomainError.InvalidChangeData
            }

            val dto = request.toDto()
            val response = dealsService.applyChange(quoteId, dto, ifMatch, idempotencyKey)

            Log.d(TAG, "✅ Cambios procesados exitosamente: changeId=${response.changeId}")
            Log.d(TAG, "   (En TRATO: mensaje CHANGE_APPLIED. En ACEPTADA: mensaje CHANGE_PROPOSED)")
            
            response.toDomain()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al aplicar cambios: ${e.message}", e)
            when {
                e.message?.contains("401") == true -> throw DealsDomainError.Unauthorized
                e.message?.contains("403") == true -> throw DealsDomainError.NotChatParticipant
                e.message?.contains("404") == true -> throw DealsDomainError.QuoteNotFound
                e.message?.contains("400") == true -> throw DealsDomainError.InvalidChangeData
                e.message?.contains("409") == true -> throw DealsDomainError.VersionConflict
                e.message?.contains("500") == true -> throw DealsDomainError.ServerError
                else -> throw DealsDomainError.NetworkError
            }
        }
    }

    override suspend fun proposeAcceptance(
        quoteId: Long,
        request: AcceptanceRequest
    ): AcceptanceResponse = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "📋 [REPOSITORY] Proponiendo aceptación")
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "   QuoteId: $quoteId")
            Log.d(TAG, "   IdempotencyKey: ${request.idempotencyKey}")
            Log.d(TAG, "   Note: ${request.note?.take(50) ?: "null"}")

            // Validar request
            if (!request.isValid()) {
                Log.e(TAG, "   ❌ Request inválido")
                throw DealsDomainError.InvalidChangeData
            }
            Log.d(TAG, "   ✅ Request válido")

            Log.d(TAG, "   Convirtiendo request a DTO...")
            val dto = request.toDto()
            Log.d(TAG, "   DTO: idempotencyKey=${dto.idempotencyKey}, note=${dto.note?.take(50)}")

            Log.d(TAG, "   Llamando a dealsService.proposeAcceptance()...")
            val response = dealsService.proposeAcceptance(quoteId, dto)
            Log.d(TAG, "   ✅ Respuesta recibida: acceptanceId=${response.acceptanceId}")

            Log.d(TAG, "   Convirtiendo DTO a dominio...")
            val domainResponse = response.toDomain()
            Log.d(TAG, "   ✅ Domain response: acceptanceId=${domainResponse.acceptanceId}")

            Log.d(TAG, "   ⚠️ Se generó mensaje ACCEPTANCE_REQUEST en el chat")
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "✅ [REPOSITORY] Propuesta creada exitosamente")
            Log.d(TAG, "═══════════════════════════════════════")
            
            domainResponse
        } catch (e: Exception) {
            Log.e(TAG, "═══════════════════════════════════════")
            Log.e(TAG, "❌ [REPOSITORY] Error al proponer aceptación")
            Log.e(TAG, "═══════════════════════════════════════")
            Log.e(TAG, "   Exception: ${e.javaClass.simpleName}")
            Log.e(TAG, "   Message: ${e.message}")
            Log.e(TAG, "   QuoteId: $quoteId")
            e.printStackTrace()
            
            val domainError = when {
                e.message?.contains("401") == true -> DealsDomainError.Unauthorized
                e.message?.contains("403") == true -> DealsDomainError.NotChatParticipant
                e.message?.contains("404") == true -> DealsDomainError.QuoteNotFound
                e.message?.contains("400") == true -> DealsDomainError.InvalidChangeData
                e.message?.contains("409") == true -> DealsDomainError.VersionConflict
                e.message?.contains("500") == true -> DealsDomainError.ServerError
                else -> DealsDomainError.NetworkError
            }
            Log.e(TAG, "   Lanzando error de dominio: ${domainError.javaClass.simpleName}")
            Log.e(TAG, "═══════════════════════════════════════")
            throw domainError
        }
    }

    override suspend fun confirmAcceptance(
        quoteId: Long,
        acceptanceId: Long
    ): ConfirmAcceptanceResponse = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "✅ [REPOSITORY] Confirmando aceptación")
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "   QuoteId: $quoteId")
            Log.d(TAG, "   AcceptanceId: $acceptanceId")

            Log.d(TAG, "   Llamando a dealsService.confirmAcceptance()...")
            val response = dealsService.confirmAcceptance(quoteId, acceptanceId)
            Log.d(TAG, "   ✅ Respuesta recibida: ok=${response.ok}")

            Log.d(TAG, "   Convirtiendo DTO a dominio...")
            val domainResponse = response.toDomain()
            Log.d(TAG, "   ✅ Domain response: ok=${domainResponse.ok}")

            Log.d(TAG, "   ⚠️ La cotización pasa a estado ACEPTADA")
            Log.d(TAG, "   ⚠️ Se generó mensaje ACCEPTANCE_CONFIRMED en el chat")
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "✅ [REPOSITORY] Aceptación confirmada exitosamente")
            Log.d(TAG, "═══════════════════════════════════════")
            
            domainResponse
        } catch (e: Exception) {
            Log.e(TAG, "═══════════════════════════════════════")
            Log.e(TAG, "❌ [REPOSITORY] Error al confirmar aceptación")
            Log.e(TAG, "═══════════════════════════════════════")
            Log.e(TAG, "   Exception: ${e.javaClass.simpleName}")
            Log.e(TAG, "   Message: ${e.message}")
            Log.e(TAG, "   QuoteId: $quoteId")
            Log.e(TAG, "   AcceptanceId: $acceptanceId")
            e.printStackTrace()
            
            val domainError = when {
                e.message?.contains("401") == true -> DealsDomainError.Unauthorized
                e.message?.contains("403") == true -> DealsDomainError.NotChatParticipant
                e.message?.contains("404") == true -> DealsDomainError.QuoteNotFound
                e.message?.contains("400") == true -> DealsDomainError.InvalidChangeData
                e.message?.contains("409") == true -> DealsDomainError.VersionConflict
                e.message?.contains("500") == true -> DealsDomainError.ServerError
                else -> DealsDomainError.NetworkError
            }
            Log.e(TAG, "   Lanzando error de dominio: ${domainError.javaClass.simpleName}")
            Log.e(TAG, "═══════════════════════════════════════")
            throw domainError
        }
    }

    override suspend fun rejectAcceptance(
        quoteId: Long,
        acceptanceId: Long
    ): ConfirmAcceptanceResponse = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "❌ [REPOSITORY] Rechazando aceptación")
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "   QuoteId: $quoteId")
            Log.d(TAG, "   AcceptanceId: $acceptanceId")

            Log.d(TAG, "   Llamando a dealsService.rejectAcceptance()...")
            val response = dealsService.rejectAcceptance(quoteId, acceptanceId)
            Log.d(TAG, "   ✅ Respuesta recibida: ok=${response.ok}")

            Log.d(TAG, "   Convirtiendo DTO a dominio...")
            val domainResponse = response.toDomain()
            Log.d(TAG, "   ✅ Domain response: ok=${domainResponse.ok}")

            Log.d(TAG, "   ⚠️ La cotización mantiene su estado actual (TRATO o EN_ESPERA)")
            Log.d(TAG, "   ⚠️ Se generó mensaje ACCEPTANCE_REJECTED en el chat")
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "✅ [REPOSITORY] Aceptación rechazada exitosamente")
            Log.d(TAG, "═══════════════════════════════════════")
            
            domainResponse
        } catch (e: Exception) {
            Log.e(TAG, "═══════════════════════════════════════")
            Log.e(TAG, "❌ [REPOSITORY] Error al rechazar aceptación")
            Log.e(TAG, "═══════════════════════════════════════")
            Log.e(TAG, "   Exception: ${e.javaClass.simpleName}")
            Log.e(TAG, "   Message: ${e.message}")
            Log.e(TAG, "   QuoteId: $quoteId")
            Log.e(TAG, "   AcceptanceId: $acceptanceId")
            e.printStackTrace()
            
            val domainError = when {
                e.message?.contains("401") == true -> DealsDomainError.Unauthorized
                e.message?.contains("403") == true -> DealsDomainError.NotChatParticipant
                e.message?.contains("404") == true -> DealsDomainError.QuoteNotFound
                e.message?.contains("400") == true -> DealsDomainError.InvalidChangeData
                e.message?.contains("409") == true -> DealsDomainError.VersionConflict
                e.message?.contains("500") == true -> DealsDomainError.ServerError
                else -> DealsDomainError.NetworkError
            }
            Log.e(TAG, "   Lanzando error de dominio: ${domainError.javaClass.simpleName}")
            Log.e(TAG, "═══════════════════════════════════════")
            throw domainError
        }
    }

    override suspend fun decisionChange(
        quoteId: Long,
        changeId: Long,
        accept: Boolean,
        ifMatch: String?
    ): Unit = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "⚙️ [REPOSITORY] Decidiendo sobre cambio")
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "   QuoteId: $quoteId")
            Log.d(TAG, "   ChangeId: $changeId")
            Log.d(TAG, "   Accept: $accept")
            Log.d(TAG, "   IfMatch: $ifMatch")

            val requestDto = ChangeDecisionRequestDto(accept = accept)
            Log.d(TAG, "   Llamando a dealsService.decisionChange()...")
            val response = dealsService.decisionChange(quoteId, changeId, requestDto, ifMatch)

            if (response.isSuccessful) {
                Log.d(TAG, "   ✅ Respuesta recibida: HTTP ${response.code()}")
                if (accept) {
                    Log.d(TAG, "   ⚠️ Cambio aceptado: se aplicaron los cambios a la cotización")
                    Log.d(TAG, "   ⚠️ Change marcado como APLICADO")
                    Log.d(TAG, "   ⚠️ Se generó mensaje CHANGE_ACCEPTED en el chat")
                } else {
                    Log.d(TAG, "   ⚠️ Cambio rechazado: NO se aplicaron cambios")
                    Log.d(TAG, "   ⚠️ Change marcado como RECHAZADO")
                    Log.d(TAG, "   ⚠️ Se generó mensaje CHANGE_REJECTED en el chat")
                }
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "✅ [REPOSITORY] Decisión aplicada exitosamente")
                Log.d(TAG, "═══════════════════════════════════════")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "   ❌ Error al decidir sobre cambio: HTTP ${response.code()} - $errorBody")
                throw when (response.code()) {
                    401 -> DealsDomainError.Unauthorized
                    403 -> DealsDomainError.NotChatParticipant
                    404 -> DealsDomainError.QuoteNotFound
                    400 -> DealsDomainError.InvalidChangeData
                    409 -> DealsDomainError.VersionConflict
                    500 -> DealsDomainError.ServerError
                    else -> DealsDomainError.NetworkError
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "═══════════════════════════════════════")
            Log.e(TAG, "❌ [REPOSITORY] Error al decidir sobre cambio")
            Log.e(TAG, "═══════════════════════════════════════")
            Log.e(TAG, "   Exception: ${e.javaClass.simpleName}")
            Log.e(TAG, "   Message: ${e.message}")
            Log.e(TAG, "   QuoteId: $quoteId")
            Log.e(TAG, "   ChangeId: $changeId")
            e.printStackTrace()
            
            val domainError = when {
                e is DealsDomainError -> e
                e.message?.contains("401") == true -> DealsDomainError.Unauthorized
                e.message?.contains("403") == true -> DealsDomainError.NotChatParticipant
                e.message?.contains("404") == true -> DealsDomainError.QuoteNotFound
                e.message?.contains("400") == true -> DealsDomainError.InvalidChangeData
                e.message?.contains("409") == true -> DealsDomainError.VersionConflict
                e.message?.contains("500") == true -> DealsDomainError.ServerError
                else -> DealsDomainError.NetworkError
            }
            Log.e(TAG, "   Lanzando error de dominio: ${domainError.javaClass.simpleName}")
            Log.e(TAG, "═══════════════════════════════════════")
            throw domainError
        }
    }

    override suspend fun getChangeDetail(
        quoteId: Long,
        changeId: Long
    ): Change = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "🔍 [REPOSITORY] Obteniendo detalle de cambio")
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "   QuoteId: $quoteId")
            Log.d(TAG, "   ChangeId: $changeId")

            Log.d(TAG, "   Llamando a dealsService.getChangeDetail()...")
            val dto = dealsService.getChangeDetail(quoteId, changeId)
            Log.d(TAG, "   ✅ Respuesta recibida: changeId=${dto.changeId}, kindCode=${dto.kindCode}, statusCode=${dto.statusCode}")

            Log.d(TAG, "   Convirtiendo DTO a dominio...")
            val domain = dto.toDomain()
            Log.d(TAG, "   ✅ Domain: changeId=${domain.changeId}, kindCode=${domain.kindCode}, statusCode=${domain.statusCode}, items=${domain.items.size}")

            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "✅ [REPOSITORY] Detalle de cambio obtenido exitosamente")
            Log.d(TAG, "═══════════════════════════════════════")
            
            domain
        } catch (e: Exception) {
            Log.e(TAG, "═══════════════════════════════════════")
            Log.e(TAG, "❌ [REPOSITORY] Error al obtener detalle de cambio")
            Log.e(TAG, "═══════════════════════════════════════")
            Log.e(TAG, "   Exception: ${e.javaClass.simpleName}")
            Log.e(TAG, "   Message: ${e.message}")
            Log.e(TAG, "   QuoteId: $quoteId")
            Log.e(TAG, "   ChangeId: $changeId")
            e.printStackTrace()
            
            val domainError = when {
                e.message?.contains("401") == true -> DealsDomainError.Unauthorized
                e.message?.contains("403") == true -> DealsDomainError.NotChatParticipant
                e.message?.contains("404") == true -> DealsDomainError.QuoteNotFound
                e.message?.contains("400") == true -> DealsDomainError.InvalidChangeData
                e.message?.contains("500") == true -> DealsDomainError.ServerError
                else -> DealsDomainError.NetworkError
            }
            Log.e(TAG, "   Lanzando error de dominio: ${domainError.javaClass.simpleName}")
            Log.e(TAG, "═══════════════════════════════════════")
            throw domainError
        }
    }
}

