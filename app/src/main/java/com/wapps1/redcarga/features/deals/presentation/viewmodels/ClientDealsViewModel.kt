package com.wapps1.redcarga.features.deals.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wapps1.redcarga.core.websocket.RedcargaWebSocketManager
import com.wapps1.redcarga.core.websocket.WebSocketMessageType
import com.wapps1.redcarga.features.requests.domain.models.QuoteDetail
import com.wapps1.redcarga.features.requests.domain.models.RequestSummary
import com.wapps1.redcarga.features.requests.domain.repositories.QuotesRepository
import com.wapps1.redcarga.features.requests.domain.repositories.RequestsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "ClientDealsVM"

@HiltViewModel
class ClientDealsViewModel @Inject constructor(
    private val requestsRepository: RequestsRepository,
    private val quotesRepository: QuotesRepository,
    private val webSocketManager: RedcargaWebSocketManager
) : ViewModel() {

    // Estados de la UI
    sealed class UiState {
        object Loading : UiState()
        data class Success(val requests: List<RequestSummary>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Observar solicitudes del cliente desde el repositorio
    val clientRequests: StateFlow<List<RequestSummary>> = requestsRepository
        .observeClientRequests()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ⭐ Cotizaciones por requestId y estado (clave: "${requestId}_${stateKey}")
    private val _quotesByRequestId = MutableStateFlow<Map<String, List<QuoteDetail>>>(emptyMap())
    val quotesByRequestId: StateFlow<Map<String, List<QuoteDetail>>> = _quotesByRequestId.asStateFlow()

    // ⭐ Estado de carga de cotizaciones (clave: "${requestId}_${stateKey}")
    private val _quotesLoadingState = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val quotesLoadingState: StateFlow<Map<String, Boolean>> = _quotesLoadingState.asStateFlow()

    // ⭐ Mensajes de acción (éxito/error)
    sealed class ActionMessage {
        data class Success(val message: String) : ActionMessage()
        data class Error(val message: String) : ActionMessage()
    }

    private val _actionMessage = MutableStateFlow<ActionMessage?>(null)
    val actionMessage: StateFlow<ActionMessage?> = _actionMessage.asStateFlow()

    // ⭐ Estado de procesamiento de acciones
    private val _processingAction = MutableStateFlow<Map<Long, Boolean>>(emptyMap())
    val processingAction: StateFlow<Map<Long, Boolean>> = _processingAction.asStateFlow()

    // ⭐ Set de timestamps de mensajes procesados para evitar duplicados
    private val processedMessageTimestamps = mutableSetOf<Long>()

    init {
        // Cargar solicitudes al iniciar
        refreshRequests()

        // ⭐ Observar mensajes WebSocket para cotizaciones en tiempo real
        viewModelScope.launch {
            Log.d(TAG, "🔌 Iniciando observación de WebSocket messages para cotizaciones...")
            webSocketManager.receivedMessages.collect { messages ->
                // ⭐ Procesar solo mensajes nuevos (no procesados antes)
                messages.forEach { message ->
                    // Verificar si ya procesamos este mensaje (por timestamp)
                    if (message.timestamp in processedMessageTimestamps) {
                        Log.d(TAG, "⏭️ Mensaje ya procesado (timestamp: ${message.timestamp}), saltando...")
                        return@forEach
                    }

                    // Marcar como procesado
                    processedMessageTimestamps.add(message.timestamp)

                    // Limpiar timestamps antiguos (mantener solo los últimos 100)
                    if (processedMessageTimestamps.size > 100) {
                        val oldest = processedMessageTimestamps.minOrNull()
                        if (oldest != null) {
                            processedMessageTimestamps.remove(oldest)
                        }
                    }

                    // ⭐ DETECTAR QUOTE_CREATED
                    if (message.type == WebSocketMessageType.QUOTE_CREATED) {
                        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        Log.d(TAG, "💰💰💰 CLIENTE: QUOTE_CREATED RECIBIDO 💰💰💰")
                        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        Log.d(TAG, "📨 Mensaje completo recibido:")
                        Log.d(TAG, "   ┌─────────────────────────────────────────────────────────")
                        Log.d(TAG, "   │ ${message.content}")
                        Log.d(TAG, "   └─────────────────────────────────────────────────────────")
                        Log.d(TAG, "⏰ Timestamp: ${message.timestamp}")
                        
                        // ⭐ Parsear el requestId y quoteId usando JSONObject
                        val requestId = parseRequestIdFromWebSocketMessage(message.content)
                        val quoteId = parseQuoteIdFromWebSocketMessage(message.content)
                        
                        if (requestId != null && quoteId != null) {
                            Log.d(TAG, "✅ requestId parseado correctamente: $requestId")
                            Log.d(TAG, "✅ quoteId parseado correctamente: $quoteId")
                            Log.d(TAG, "🔄 Agregando cotización nueva al principio de la lista...")
                            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                            
                            // ⭐ Obtener solo la cotización nueva y ponerla al principio
                            addNewQuoteToTop(quoteId, requestId)
                        } else if (requestId != null) {
                            // Fallback: si no podemos obtener quoteId, refrescar todo
                            Log.d(TAG, "⚠️ No se pudo parsear quoteId, refrescando todas las cotizaciones...")
                            loadQuotesForRequestWithRetry(requestId, isFromWebSocket = true)
                        } else {
                            Log.w(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                            Log.w(TAG, "⚠️ ERROR: No se pudo parsear requestId del mensaje WebSocket")
                            Log.w(TAG, "   Contenido recibido: ${message.content}")
                            Log.w(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        }
                    }
                }
            }
        }
    }

    // Refrescar solicitudes desde el servidor
    fun refreshRequests() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                requestsRepository.refreshClientRequests()
                _uiState.value = UiState.Success(clientRequests.value)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.message ?: "Error al cargar las solicitudes"
                )
            }
        }
    }

    /**
     * Obtiene las cotizaciones de una solicitud específica con sus detalles completos
     * @param state Opcional: PENDING, TRATO, RECHAZADA. Si es null, retorna todas las cotizaciones.
     */
    fun loadQuotesForRequest(requestId: Long, state: String? = null) {
        viewModelScope.launch {
            try {
                val stateKey = state ?: "ALL"
                Log.d(TAG, "📋 Cargando cotizaciones para requestId=$requestId, state=$state")

                // Marcar como cargando
                val loadingKey = "${requestId}_$stateKey"
                _quotesLoadingState.value = _quotesLoadingState.value.toMutableMap().apply {
                    put(loadingKey, true)
                }

                // Obtener cotizaciones con detalles
                val quotes = quotesRepository.getQuotesByRequestId(requestId, state)

                // ⭐ INVERTIR LA LISTA para tener lo más reciente primero
                val reversedQuotes = quotes.reversed()

                Log.d(TAG, "✅✅ Cotizaciones cargadas: ${reversedQuotes.size} items para requestId=$requestId, state=$state (orden invertido: más reciente primero)")

                // Guardar en el mapa con clave que incluye el estado
                val quotesKey = "${requestId}_$stateKey"
                _quotesByRequestId.value = _quotesByRequestId.value.toMutableMap().apply {
                    put(quotesKey, reversedQuotes)
                }

                // Marcar como completado
                _quotesLoadingState.value = _quotesLoadingState.value.toMutableMap().apply {
                    put(loadingKey, false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al cargar cotizaciones para requestId=$requestId, state=$state", e)

                val stateKey = state ?: "ALL"
                val loadingKey = "${requestId}_$stateKey"

                // Marcar como completado (con error)
                _quotesLoadingState.value = _quotesLoadingState.value.toMutableMap().apply {
                    put(loadingKey, false)
                }

                // Guardar lista vacía en caso de error
                val quotesKey = "${requestId}_$stateKey"
                _quotesByRequestId.value = _quotesByRequestId.value.toMutableMap().apply {
                    put(quotesKey, emptyList())
                }
            }
        }
    }

    /**
     * Obtiene las cotizaciones de una solicitud específica con un estado determinado
     */
    fun getQuotesForRequest(requestId: Long, state: String? = null): List<QuoteDetail> {
        val stateKey = state ?: "ALL"
        val quotesKey = "${requestId}_$stateKey"
        return _quotesByRequestId.value[quotesKey] ?: emptyList()
    }

    /**
     * Verifica si se están cargando cotizaciones para una solicitud con un estado determinado
     */
    fun isLoadingQuotes(requestId: Long, state: String? = null): Boolean {
        val stateKey = state ?: "ALL"
        val loadingKey = "${requestId}_$stateKey"
        return _quotesLoadingState.value[loadingKey] ?: false
    }


    /**
     * Inicia la negociación de una cotización
     */
    fun startNegotiation(quoteId: Long, requestId: Long) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🤝 Iniciando negociación para quoteId=$quoteId")

                // Marcar como procesando
                _processingAction.value = _processingAction.value.toMutableMap().apply {
                    put(quoteId, true)
                }

                val result = quotesRepository.startNegotiation(quoteId)

                if (result.isSuccess) {
                    Log.d(TAG, "✅✅ Negociación iniciada exitosamente")
                    _actionMessage.value = ActionMessage.Success(
                        "¡Trato iniciado correctamente! Ahora podrás comunicarte con el proveedor para coordinar temas del servicio, precios, etc."
                    )

                    // Refrescar cotizaciones para actualizar el estado (todas las variantes)
                    loadQuotesForRequest(requestId, null) // Todas
                    loadQuotesForRequest(requestId, "PENDING")
                    loadQuotesForRequest(requestId, "TRATO")
                    loadQuotesForRequest(requestId, "RECHAZADA")
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Log.e(TAG, "❌ Error al iniciar negociación: $error")
                    _actionMessage.value = ActionMessage.Error(
                        "El trato no se inició correctamente. Por favor, intenta nuevamente."
                    )
                }

                // Marcar como completado
                _processingAction.value = _processingAction.value.toMutableMap().apply {
                    put(quoteId, false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al iniciar negociación", e)
                _actionMessage.value = ActionMessage.Error(
                    "El trato no se inició correctamente. Por favor, intenta nuevamente."
                )
                _processingAction.value = _processingAction.value.toMutableMap().apply {
                    put(quoteId, false)
                }
            }
        }
    }

    /**
     * Rechaza una cotización
     */
    fun rejectQuote(quoteId: Long, requestId: Long) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "❌ Rechazando cotización quoteId=$quoteId")

                // Marcar como procesando
                _processingAction.value = _processingAction.value.toMutableMap().apply {
                    put(quoteId, true)
                }

                val result = quotesRepository.rejectQuote(quoteId)

                if (result.isSuccess) {
                    Log.d(TAG, "✅✅ Cotización rechazada exitosamente")
                    _actionMessage.value = ActionMessage.Success(
                        "Cotización rechazada correctamente."
                    )

                    // Refrescar cotizaciones para actualizar la lista (todas las variantes)
                    loadQuotesForRequest(requestId, null) // Todas
                    loadQuotesForRequest(requestId, "PENDING")
                    loadQuotesForRequest(requestId, "TRATO")
                    loadQuotesForRequest(requestId, "RECHAZADA")
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Log.e(TAG, "❌ Error al rechazar cotización: $error")
                    _actionMessage.value = ActionMessage.Error(
                        "No se pudo rechazar la cotización. Por favor, intenta nuevamente."
                    )
                }

                // Marcar como completado
                _processingAction.value = _processingAction.value.toMutableMap().apply {
                    put(quoteId, false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al rechazar cotización", e)
                _actionMessage.value = ActionMessage.Error(
                    "No se pudo rechazar la cotización. Por favor, intenta nuevamente."
                )
                _processingAction.value = _processingAction.value.toMutableMap().apply {
                    put(quoteId, false)
                }
            }
        }
    }

    /**
     * Limpia el mensaje de acción
     */
    fun clearActionMessage() {
        _actionMessage.value = null
    }

    /**
     * Verifica si se está procesando una acción para una cotización
     */
    fun isProcessingAction(quoteId: Long): Boolean {
        return _processingAction.value[quoteId] ?: false
    }

    /**
     * ⭐ Refresca las cotizaciones con delay y retry para evitar race conditions
     * @param requestId ID de la solicitud
     * @param isFromWebSocket Si es true, agrega delay y retry para evitar race conditions
     */
    private fun loadQuotesForRequestWithRetry(requestId: Long, isFromWebSocket: Boolean = false) {
        Log.d(TAG, "🔄 loadQuotesForRequestWithRetry() llamado (requestId=$requestId, isFromWebSocket=$isFromWebSocket)")
        viewModelScope.launch {
            try {
                // ⭐ Delay si viene de WebSocket para dar tiempo al backend
                if (isFromWebSocket) {
                    Log.d(TAG, "⏳ Esperando 1 segundo antes de refrescar (evitar race condition)...")
                    delay(1000) // 1 segundo de delay
                }

                Log.d(TAG, "🔄 Refrescando cotizaciones para requestId=$requestId...")

                // Refrescar todas las variantes de estado
                loadQuotesForRequest(requestId, null) // Todas
                loadQuotesForRequest(requestId, "PENDING")
                loadQuotesForRequest(requestId, "TRATO")
                loadQuotesForRequest(requestId, "RECHAZADA")

                Log.d(TAG, "✅✅ Cotizaciones refrescadas correctamente para requestId=$requestId")
            } catch (e: Exception) {
                Log.e(TAG, "❌❌ ERROR al refrescar cotizaciones después de WebSocket", e)
                Log.e(TAG, "   Tipo: ${e::class.simpleName}")
                Log.e(TAG, "   Mensaje: ${e.message}")

                // ⭐ Si viene de WebSocket y falla, intentar retry
                if (isFromWebSocket) {
                    // ⭐ Retry después de 2 segundos (solo una vez)
                    Log.d(TAG, "🔄 Intentando retry en 2 segundos...")
                    delay(2000)

                    try {
                        Log.d(TAG, "🔄 Retry: Refrescando cotizaciones...")
                        loadQuotesForRequest(requestId, null)
                        loadQuotesForRequest(requestId, "PENDING")
                        loadQuotesForRequest(requestId, "TRATO")
                        loadQuotesForRequest(requestId, "RECHAZADA")
                        Log.d(TAG, "✅✅ Retry exitoso - Cotizaciones refrescadas")
                    } catch (retryException: Exception) {
                        Log.e(TAG, "❌❌ Retry también falló", retryException)
                    }
                }
            }
        }
    }

    /**
     * ⭐ Parsea el requestId del mensaje WebSocket usando JSONObject
     * El mensaje viene en formato JSON: {"type":"QUOTE_CREATED","requestId": 123, "quoteId": 456, ...}
     */
    private fun parseRequestIdFromWebSocketMessage(content: String?): Long? {
        if (content.isNullOrBlank()) return null

        return try {
            // ⭐ Intentar parsear como JSON primero
            if (content.trim().startsWith("{") && content.trim().endsWith("}")) {
                val json = JSONObject(content)
                val requestId = json.optLong("requestId", -1L)

                if (requestId > 0) {
                    Log.d(TAG, "✅ requestId parseado correctamente: $requestId")
                    return requestId
                } else {
                    Log.w(TAG, "⚠️ requestId no encontrado o inválido en JSON")
                }
            }

            // ⭐ Fallback: usar regex si el JSON no funciona
            val regex = """"requestId"\s*:\s*(\d+)""".toRegex()
            val matchResult = regex.find(content)
            val parsedId = matchResult?.groupValues?.get(1)?.toLongOrNull()

            if (parsedId != null) {
                Log.d(TAG, "✅ requestId parseado con regex: $parsedId")
            } else {
                Log.w(TAG, "⚠️ No se pudo parsear requestId ni con JSON ni con regex")
            }

            parsedId
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Error al parsear requestId del mensaje WebSocket: ${e.message}")
            Log.w(TAG, "   Contenido: ${content.take(200)}")
            null
        }
    }

    /**
     * ⭐ Parsea el quoteId del mensaje WebSocket usando JSONObject
     * El mensaje viene en formato JSON: {"type":"QUOTE_CREATED","requestId": 123, "quoteId": 456, ...}
     */
    private fun parseQuoteIdFromWebSocketMessage(content: String?): Long? {
        if (content.isNullOrBlank()) return null

        return try {
            // ⭐ Intentar parsear como JSON primero
            if (content.trim().startsWith("{") && content.trim().endsWith("}")) {
                val json = JSONObject(content)
                val quoteId = json.optLong("quoteId", -1L)

                if (quoteId > 0) {
                    Log.d(TAG, "✅ quoteId parseado correctamente: $quoteId")
                    return quoteId
                } else {
                    Log.w(TAG, "⚠️ quoteId no encontrado o inválido en JSON")
                }
            }

            // ⭐ Fallback: usar regex si el JSON no funciona
            val regex = """"quoteId"\s*:\s*(\d+)""".toRegex()
            val matchResult = regex.find(content)
            val parsedId = matchResult?.groupValues?.get(1)?.toLongOrNull()

            if (parsedId != null) {
                Log.d(TAG, "✅ quoteId parseado con regex: $parsedId")
            } else {
                Log.w(TAG, "⚠️ No se pudo parsear quoteId ni con JSON ni con regex")
            }

            parsedId
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Error al parsear quoteId del mensaje WebSocket: ${e.message}")
            Log.w(TAG, "   Contenido: ${content.take(200)}")
            null
        }
    }

    /**
     * ⭐ Obtiene una cotización nueva por WebSocket y la agrega al principio de la lista
     * @param quoteId ID de la cotización nueva
     * @param requestId ID de la solicitud
     */
    private fun addNewQuoteToTop(quoteId: Long, requestId: Long) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "📥 Obteniendo detalle de cotización nueva: quoteId=$quoteId")
                
                // Obtener el detalle de la cotización nueva
                val newQuote = quotesRepository.getQuoteDetail(quoteId)
                
                Log.d(TAG, "✅ Cotización nueva obtenida: quoteId=${newQuote.quoteId}, requestId=${newQuote.requestId}")
                
                // ⭐ Actualizar todas las variantes de estado (ALL, PENDING, TRATO, RECHAZADA)
                val states = listOf(null, "PENDING", "TRATO", "RECHAZADA")
                
                states.forEach { state ->
                    val stateKey = state ?: "ALL"
                    val quotesKey = "${requestId}_$stateKey"
                    
                    // Obtener la lista actual
                    val currentQuotes = _quotesByRequestId.value[quotesKey] ?: emptyList()
                    
                    // Verificar si la cotización ya existe en la lista (por si acaso)
                    val quoteExists = currentQuotes.any { it.quoteId == quoteId }
                    
                    if (!quoteExists) {
                        // ⭐ Crear nueva lista con la cotización nueva al principio
                        val updatedQuotes = listOf(newQuote) + currentQuotes
                        
                        Log.d(TAG, "📝 Agregando cotización nueva al principio (state=$stateKey): ${updatedQuotes.size} cotizaciones totales")
                        
                        // Actualizar el mapa
                        _quotesByRequestId.value = _quotesByRequestId.value.toMutableMap().apply {
                            put(quotesKey, updatedQuotes)
                        }
                    } else {
                        Log.d(TAG, "ℹ️ La cotización quoteId=$quoteId ya existe en la lista (state=$stateKey), omitiendo...")
                    }
                }
                
                Log.d(TAG, "✅✅ Cotización nueva agregada al principio de todas las listas")
            } catch (e: Exception) {
                Log.e(TAG, "❌❌ ERROR al obtener y agregar cotización nueva", e)
                Log.e(TAG, "   Tipo: ${e::class.simpleName}, Mensaje: ${e.message}")
                
                // ⭐ Fallback: si falla, refrescar todas las cotizaciones
                Log.d(TAG, "🔄 Fallback: Refrescando todas las cotizaciones...")
                loadQuotesForRequestWithRetry(requestId, isFromWebSocket = true)
            }
        }
    }
}


