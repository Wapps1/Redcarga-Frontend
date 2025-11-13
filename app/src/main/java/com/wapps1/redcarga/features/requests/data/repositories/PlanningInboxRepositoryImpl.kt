package com.wapps1.redcarga.features.requests.data.repositories

import android.util.Log
import com.wapps1.redcarga.features.requests.data.local.dao.IncomingRequestsDao
import com.wapps1.redcarga.features.requests.data.mappers.toDomain
import com.wapps1.redcarga.features.requests.data.mappers.toEntity
import com.wapps1.redcarga.features.requests.data.mappers.RequestsMappers
import com.wapps1.redcarga.features.requests.data.remote.services.PlanningInboxService
import com.wapps1.redcarga.features.requests.data.remote.services.RequestsService
import com.wapps1.redcarga.features.requests.domain.models.IncomingRequestSummary
import com.wapps1.redcarga.features.requests.domain.models.Request
import com.wapps1.redcarga.features.requests.domain.repositories.PlanningInboxRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PlanningInboxRepo"

/**
 * Implementación del repositorio de inbox de solicitudes para proveedores
 */
@Singleton
class PlanningInboxRepositoryImpl @Inject constructor(
    private val inboxService: PlanningInboxService,
    private val requestsService: RequestsService,
    private val inboxDao: IncomingRequestsDao
) : PlanningInboxRepository {

    override fun observeIncomingRequests(companyId: Long): Flow<List<IncomingRequestSummary>> {
        Log.d(TAG, "📡 observeIncomingRequests(companyId=$companyId)")
        return inboxDao.observeInbox(companyId).map { entities ->
            Log.d(TAG, "📦 Room DB emitió ${entities.size} solicitudes")
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshIncomingRequests(companyId: Long): Unit = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔄 refreshIncomingRequests(companyId=$companyId)")
        try {
            Log.d(TAG, "🌐 Llamando GET /planning/companies/$companyId/request-inbox")

            // Fetch desde el backend
            val dtos = inboxService.getRequestInbox(companyId)

            Log.d(TAG, "✅ Backend respondió: ${dtos.size} solicitudes")
            if (dtos.isNotEmpty()) {
                Log.d(TAG, "   Primera solicitud: requestId=${dtos.first().requestId}, requester=${dtos.first().requesterName}")
            }

            // Convertir a entities
            Log.d(TAG, "💾 Convirtiendo ${dtos.size} DTOs a entities...")
            val entities = dtos.map { it.toEntity() }

            // Guardar en Room (reemplazar todas)
            Log.d(TAG, "💾 Guardando en Room Database...")
            inboxDao.replaceAll(companyId, entities)

            Log.d(TAG, "✅✅ REFRESH COMPLETO - ${entities.size} solicitudes en DB")
        } catch (e: Exception) {
            Log.e(TAG, "❌❌ ERROR al refrescar solicitudes", e)
            Log.e(TAG, "   Tipo: ${e::class.simpleName}, Mensaje: ${e.message}")
            throw e
        }
    }

    override suspend fun getRequestDetail(requestId: Long): Request = withContext(Dispatchers.IO) {
        Log.d(TAG, "getRequestDetail(requestId=$requestId)")
        try {
            // Reutiliza el endpoint de RequestsService
            val dto = requestsService.getRequestById(requestId)
            val domain = RequestsMappers.run { dto.toDomain() }
            Log.d(TAG, "✅ Detalles obtenidos para request $requestId")
            domain
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener detalles de request $requestId", e)
            throw e
        }
    }
}

