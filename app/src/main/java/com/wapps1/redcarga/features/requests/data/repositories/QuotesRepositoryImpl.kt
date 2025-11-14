package com.wapps1.redcarga.features.requests.data.repositories

import android.util.Log
import com.wapps1.redcarga.features.requests.data.local.dao.QuotesDao
import com.wapps1.redcarga.features.requests.data.mappers.QuoteMappers
import com.wapps1.redcarga.features.requests.data.mappers.toDomain as quoteSummaryEntityToDomain
import com.wapps1.redcarga.features.requests.data.mappers.toEntity as quoteSummaryDtoToEntity
import com.wapps1.redcarga.features.requests.data.remote.services.QuotesService
import com.wapps1.redcarga.features.requests.domain.models.CreateQuoteRequest
import com.wapps1.redcarga.features.requests.domain.models.CreateQuoteResponse
import com.wapps1.redcarga.features.requests.domain.models.QuoteDetail
import com.wapps1.redcarga.features.requests.domain.models.QuoteSummary
import com.wapps1.redcarga.features.requests.domain.repositories.QuotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "QuotesRepository"

/**
 * Implementación del repository de cotizaciones
 */
@Singleton
class QuotesRepositoryImpl @Inject constructor(
    private val quotesService: QuotesService,
    private val quotesDao: QuotesDao
) : QuotesRepository {

    override suspend fun createQuote(request: CreateQuoteRequest): CreateQuoteResponse = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📝 Creando cotización para requestId=${request.requestId}")
            Log.d(TAG, "   companyId: ${request.companyId}")
            Log.d(TAG, "   totalAmount: ${request.totalAmount}")
            Log.d(TAG, "   currency: ${request.currency}")
            Log.d(TAG, "   items: ${request.items.size}")

            val dto = QuoteMappers.run { request.toDto() }
            val response = quotesService.createQuote(dto)

            Log.d(TAG, "✅ Cotización creada exitosamente:")
            Log.d(TAG, "   quoteId: ${response.quoteId}")

            // Refrescar la lista de cotizaciones después de crear una nueva
            Log.d(TAG, "🔄 Refrescando lista de cotizaciones tras crear nueva...")
            refreshQuotesByCompany(request.companyId)

            QuoteMappers.run { response.toDomain() }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al crear cotización", e)
            throw e
        }
    }

    override fun observeQuotesByCompany(companyId: Long): Flow<List<QuoteSummary>> {
        Log.d(TAG, "📡 observeQuotesByCompany(companyId=$companyId)")
        return quotesDao.observeQuotesByCompany(companyId).map { entities ->
            Log.d(TAG, "📦 Room DB emitió ${entities.size} cotizaciones")
            entities.map { it.quoteSummaryEntityToDomain() }
        }
    }

    override suspend fun refreshQuotesByCompany(companyId: Long): Unit = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔄 refreshQuotesByCompany(companyId=$companyId)")
        try {
            Log.d(TAG, "🌐 Llamando GET /api/deals/quotes/general?company_id=$companyId")

            // Fetch desde el backend
            val dtos = quotesService.getQuotesByCompany(companyId)

            Log.d(TAG, "✅ Backend respondió: ${dtos.size} cotizaciones")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "📋 LISTA COMPLETA DE COTIZACIONES:")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            dtos.forEachIndexed { index, dto ->
                Log.d(TAG, "[$index] QuoteID: ${dto.quoteId}")
                Log.d(TAG, "    ├─ RequestID: ${dto.requestId}")
                Log.d(TAG, "    ├─ Monto: ${dto.totalAmount} ${dto.currencyCode}")
                Log.d(TAG, "    └─ Fecha: ${dto.createdAt}")
                Log.d(TAG, "")
            }
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Convertir a entities
            Log.d(TAG, "💾 Convirtiendo ${dtos.size} DTOs a entities...")
            val entities = dtos.map { it.quoteSummaryDtoToEntity() }

            // Guardar en Room (reemplazar todas)
            Log.d(TAG, "💾 Guardando en Room Database...")
            quotesDao.replaceAll(companyId, entities)

            Log.d(TAG, "✅✅ REFRESH COMPLETO - ${entities.size} cotizaciones en DB")
        } catch (e: Exception) {
            Log.e(TAG, "❌❌ ERROR al refrescar cotizaciones", e)
            Log.e(TAG, "   Tipo: ${e::class.simpleName}, Mensaje: ${e.message}")
            throw e
        }
    }

    override suspend fun getQuoteDetail(quoteId: Long): QuoteDetail = withContext(Dispatchers.IO) {
        Log.d(TAG, "getQuoteDetail(quoteId=$quoteId)")
        try {
            val dto = quotesService.getQuoteDetail(quoteId)
            Log.d(TAG, "✅ Detalles obtenidos para quote $quoteId")
            QuoteMappers.run { dto.toDomain() }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener detalles de quote $quoteId", e)
            throw e
        }
    }

    override suspend fun getQuotesByRequestId(requestId: Long, state: String?): List<QuoteDetail> = withContext(Dispatchers.IO) {
        val stateParam = if (state != null) "&state=$state" else ""
        Log.d(TAG, "📋 getQuotesByRequestId(requestId=$requestId, state=$state)")
        try {
            // Paso 1: Obtener la lista de cotizaciones (resumen)
            Log.d(TAG, "🌐 Paso 1: Llamando GET /api/deals/quotes?requestId=$requestId$stateParam")

            val summaryDtos = quotesService.getQuotesByRequestId(requestId, state)

            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "📋 RESPUESTA DEL ENDPOINT /api/deals/quotes?requestId=$requestId$stateParam")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "📦 Total de cotizaciones: ${summaryDtos.size}")
            Log.d(TAG, "")

            // Imprimir cada cotización recibida
            summaryDtos.forEachIndexed { index, dto ->
                Log.d(TAG, "[$index] QuoteSummaryDto:")
                Log.d(TAG, "   quoteId: ${dto.quoteId}")
                Log.d(TAG, "   requestId: ${dto.requestId}")
                Log.d(TAG, "   companyId: ${dto.companyId}")
                Log.d(TAG, "   totalAmount: ${dto.totalAmount}")
                Log.d(TAG, "   currencyCode: ${dto.currencyCode}")
                Log.d(TAG, "   createdAt: ${dto.createdAt}")
                Log.d(TAG, "")
            }

            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Paso 2: Obtener el detalle completo de cada cotización
            Log.d(TAG, "🔍 Paso 2: Obteniendo detalles completos de cada cotización...")
            Log.d(TAG, "")

            val quoteDetails = mutableListOf<QuoteDetail>()

            summaryDtos.forEachIndexed { index, summaryDto ->
                try {
                    Log.d(TAG, "📥 [$index/${summaryDtos.size}] Obteniendo detalle de quoteId=${summaryDto.quoteId}...")

                    val detailDto = quotesService.getQuoteDetail(summaryDto.quoteId)

                    Log.d(TAG, "   ✅ Detalle obtenido:")
                    Log.d(TAG, "      quoteId: ${detailDto.quoteId}")
                    Log.d(TAG, "      requestId: ${detailDto.requestId}")
                    Log.d(TAG, "      companyId: ${detailDto.companyId}")
                    Log.d(TAG, "      createdByAccountId: ${detailDto.createdByAccountId}")
                    Log.d(TAG, "      stateCode: ${detailDto.stateCode}")
                    Log.d(TAG, "      currencyCode: ${detailDto.currencyCode}")
                    Log.d(TAG, "      totalAmount: ${detailDto.totalAmount}")
                    Log.d(TAG, "      version: ${detailDto.version}")
                    Log.d(TAG, "      createdAt: ${detailDto.createdAt}")
                    Log.d(TAG, "      updatedAt: ${detailDto.updatedAt}")
                    Log.d(TAG, "      items: ${detailDto.items.size} items")

                    // Imprimir items
                    detailDto.items.forEachIndexed { itemIndex, item ->
                        Log.d(TAG, "         [$itemIndex] quoteItemId=${item.quoteItemId}, requestItemId=${item.requestItemId}, qty=${item.qty}")
                    }

                    val detail = QuoteMappers.run { detailDto.toDomain() }
                    quoteDetails.add(detail)

                    Log.d(TAG, "")
                } catch (e: Exception) {
                    Log.e(TAG, "   ❌ Error al obtener detalle de quoteId=${summaryDto.quoteId}", e)
                    // Continuar con las demás cotizaciones aunque una falle
                }
            }

            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "✅✅ getQuotesByRequestId completado - ${quoteDetails.size} cotizaciones con detalles")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            quoteDetails
        } catch (e: Exception) {
            Log.e(TAG, "❌❌ ERROR al obtener cotizaciones por requestId", e)
            Log.e(TAG, "   Tipo: ${e::class.simpleName}, Mensaje: ${e.message}")
            throw e
        }
    }

    override suspend fun startNegotiation(quoteId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🤝 Iniciando negociación para quoteId=$quoteId")
            Log.d(TAG, "🌐 Llamando POST /api/deals/quotes/$quoteId:start-negotiation")
            Log.d(TAG, "   Header: If-Match: 0")

            val response = quotesService.startNegotiation(quoteId, "0")

            if (response.isSuccessful) {
                Log.d(TAG, "✅✅ Negociación iniciada exitosamente para quoteId=$quoteId")
                Result.success(Unit)
            } else {
                val errorMessage = "Error al iniciar negociación: ${response.code()} ${response.message()}"
                Log.e(TAG, "❌ $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌❌ ERROR al iniciar negociación para quoteId=$quoteId", e)
            Log.e(TAG, "   Tipo: ${e::class.simpleName}, Mensaje: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun rejectQuote(quoteId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "❌ Rechazando cotización quoteId=$quoteId")
            Log.d(TAG, "🌐 Llamando POST /api/deals/quotes/$quoteId:reject")

            val response = quotesService.rejectQuote(quoteId)

            if (response.isSuccessful) {
                Log.d(TAG, "✅✅ Cotización rechazada exitosamente para quoteId=$quoteId")
                Result.success(Unit)
            } else {
                val errorMessage = "Error al rechazar cotización: ${response.code()} ${response.message()}"
                Log.e(TAG, "❌ $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌❌ ERROR al rechazar cotización para quoteId=$quoteId", e)
            Log.e(TAG, "   Tipo: ${e::class.simpleName}, Mensaje: ${e.message}")
            Result.failure(e)
        }
    }
}

