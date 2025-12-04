package com.wapps1.redcarga.features.requests.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wapps1.redcarga.core.session.AuthSessionStore
import com.wapps1.redcarga.core.websocket.RedcargaWebSocketManager
import com.wapps1.redcarga.core.websocket.WebSocketMessageType
import com.wapps1.redcarga.features.requests.domain.models.IncomingRequestSummary
import com.wapps1.redcarga.features.requests.domain.models.Request
import com.wapps1.redcarga.features.requests.domain.models.QuoteSummary
import com.wapps1.redcarga.features.requests.domain.repositories.PlanningInboxRepository
import com.wapps1.redcarga.features.requests.domain.repositories.QuotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "ProviderInboxVM"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProviderIncomingRequestsViewModel @Inject constructor(
    private val inboxRepository: PlanningInboxRepository,
    private val quotesRepository: QuotesRepository,
    private val authSessionStore: AuthSessionStore,
    private val webSocketManager: RedcargaWebSocketManager
) : ViewModel() {

    // Estados de la UI
    sealed class UiState {
        object Loading : UiState()
        data class Success(val requests: List<IncomingRequestSummary>) : UiState()
        data class Error(val message: String) : UiState()
    }

    // Estados del modal de detalles
    sealed class DetailState {
        object Idle : DetailState()
        object Loading : DetailState()
        data class Success(val request: Request) : DetailState()
        data class Error(val message: String) : DetailState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow<DetailState>(DetailState.Idle)
    val detailState: StateFlow<DetailState> = _detailState.asStateFlow()

    private val _companyId = MutableStateFlow<Long?>(null)

    // Estado para notificaciones de nuevas solicitudes
    private val _newRequestNotification = MutableStateFlow<String?>(null)
    val newRequestNotification: StateFlow<String?> = _newRequestNotification.asStateFlow()

    // ID de la última solicitud nueva para destacarla
    private val _lastNewRequestId = MutableStateFlow<Long?>(null)
    val lastNewRequestId: StateFlow<Long?> = _lastNewRequestId.asStateFlow()
    
    // ⭐ MEJORADO: Set de timestamps de mensajes procesados para evitar duplicados
    private val processedMessageTimestamps = mutableSetOf<Long>()
    
    // ⭐ MEJORADO: Estado para errores de refresh después de notificación WebSocket
    private val _refreshErrorAfterNotification = MutableStateFlow<String?>(null)
    val refreshErrorAfterNotification: StateFlow<String?> = _refreshErrorAfterNotification.asStateFlow()

    // Observar solicitudes entrantes desde el repositorio
    val incomingRequests: StateFlow<List<IncomingRequestSummary>> =
        authSessionStore.currentCompanyId
            .flatMapLatest { companyId ->
                Log.d(TAG, "🔑 CompanyId cambió a: $companyId")
                _companyId.value = companyId
                if (companyId != null) {
                    Log.d(TAG, "✅ CompanyId válido ($companyId), observando repositorio...")
                    inboxRepository.observeIncomingRequests(companyId)
                } else {
                    Log.w(TAG, "⚠️ CompanyId es NULL, retornando lista vacía")
                    flowOf(emptyList())
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    
    // ⭐ Observar cotizaciones de la compañía
    private val _myQuotes = MutableStateFlow<List<QuoteSummary>>(emptyList())
    val myQuotes: StateFlow<List<QuoteSummary>> = _myQuotes.asStateFlow()
    
    // ⭐ Set de IDs de solicitudes YA cotizadas
    private val _quotedRequestIds = MutableStateFlow<Set<Long>>(emptySet())
    val quotedRequestIds: StateFlow<Set<Long>> = _quotedRequestIds.asStateFlow()
    
    // ⭐ Mapa de requestId -> stateCode para quotes aceptadas (TRATO, ACEPTADA, CERRADA)
    private val _acceptedQuotesState = MutableStateFlow<Map<Long, String>>(emptyMap())
    val acceptedQuotesState: StateFlow<Map<Long, String>> = _acceptedQuotesState.asStateFlow()
    
    // ⭐ Bandera para indicar si la carga inicial está en progreso
    private var isInitialLoadInProgress = false

    init {
        Log.d(TAG, "🎬 ViewModel inicializado")

        // Observar cambios en las solicitudes para actualizar el UI state
        viewModelScope.launch {
            Log.d(TAG, "🔄 Iniciando observación de incomingRequests...")
            incomingRequests.collect { requests ->
                Log.d(TAG, "📥 incomingRequests emitió: ${requests.size} solicitudes")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "📱 SOLICITUDES EN LA UI (ViewModel):")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                requests.forEachIndexed { index, req ->
                    Log.d(TAG, "[$index] RequestID: ${req.requestId}")
                    Log.d(TAG, "    ├─ Solicitante: ${req.requesterName}")
                    Log.d(TAG, "    ├─ Estado: ${req.status}")
                    Log.d(TAG, "    ├─ Ruta: ${req.getRouteDescription()}")
                    Log.d(TAG, "    └─ Items: ${req.totalQuantity}")
                }
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                
                val currentCompanyId = _companyId.value
                if (currentCompanyId != null) {
                    // ⭐ NO actualizar el estado a Success durante la carga inicial
                    // Solo loadAllData() debe actualizar el estado cuando termine
                    if (!isInitialLoadInProgress) {
                        _uiState.value = UiState.Success(requests)
                        Log.d(TAG, "✅ UI actualizado a Success con ${requests.size} items (companyId=$currentCompanyId)")
                    } else {
                        Log.d(TAG, "⏳ Carga inicial en progreso - No actualizar UI todavía")
                    }
                } else {
                    Log.w(TAG, "⚠️ No se actualiza UI porque companyId sigue siendo null")
                }
            }
        }

        // ⭐ MEJORADO: Observar mensajes WebSocket para auto-refresh (evita duplicados)
        viewModelScope.launch {
            Log.d(TAG, "🔌 Iniciando observación de WebSocket messages...")
            webSocketManager.receivedMessages.collect { messages ->
                // ⭐ MEJORADO: Procesar solo mensajes nuevos (no procesados antes)
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
                    
                    // Procesar solo mensajes NEW_REQUEST
                    if (message.type == WebSocketMessageType.NEW_REQUEST) {
                        Log.d(TAG, "🔔 Nueva solicitud detectada via WebSocket!")
                        Log.d(TAG, "📨 Contenido: ${message.content}")
                        Log.d(TAG, "⏰ Timestamp: ${message.timestamp}")
                        
                        // ⭐ MEJORADO: Parsear el requestId usando JSONObject
                        val requestId = parseRequestIdFromWebSocketMessage(message.content)
                        if (requestId != null) {
                            _lastNewRequestId.value = requestId
                            Log.d(TAG, "🆕 Marcando request $requestId como nueva")
                            
                            // Auto-limpiar después de 15 segundos
                            launch {
                                delay(15000)
                                if (_lastNewRequestId.value == requestId) {
                                    _lastNewRequestId.value = null
                                    Log.d(TAG, "⏰ Limpiando marca de nueva solicitud")
                                }
                            }
                        }
                        
                        // Mostrar notificación
                        _newRequestNotification.value = "¡Nueva solicitud recibida! 🎉"
                        
                        // ⭐ MEJORADO: Refrescar con delay y retry para evitar race conditions
                        refreshRequestsWithRetry(isFromWebSocket = true)
                        
                        // Auto-ocultar notificación después de 5 segundos
                        launch {
                            delay(5000)
                            _newRequestNotification.value = null
                        }
                    }
                }
            }
        }

        // Observar companyId para cargar inicialmente cuando esté disponible
        // SOLO hacemos refresh cuando el companyId pasa de null a un valor válido
        viewModelScope.launch {
            Log.d(TAG, "👀 Iniciando observación de currentCompanyId...")
            var lastCompanyId: Long? = null
            authSessionStore.currentCompanyId.collect { companyId ->
                Log.d(TAG, "🔑 currentCompanyId emitió: $companyId (anterior: $lastCompanyId)")
                if (companyId != null && lastCompanyId == null) {
                    Log.d(TAG, "✅ CompanyId disponible por primera vez: $companyId - Iniciando refresh...")
                    _uiState.value = UiState.Loading
                    loadAllData() // ⭐ Cargar TODOS los endpoints antes de mostrar la UI
                } else if (companyId == null) {
                    Log.w(TAG, "⚠️ currentCompanyId es null")
                }
                lastCompanyId = companyId
            }
        }
        
        // ⭐ Observar cotizaciones de la compañía
        viewModelScope.launch {
            Log.d(TAG, "💰 Iniciando observación de cotizaciones...")
            authSessionStore.currentCompanyId
                .flatMapLatest { companyId ->
                    if (companyId != null) {
                        Log.d(TAG, "💰 Observando cotizaciones para companyId=$companyId")
                        quotesRepository.observeQuotesByCompany(companyId)
                    } else {
                        flowOf(emptyList())
                    }
                }
                .collect { quotes ->
                    Log.d(TAG, "💰 Cotizaciones emitidas: ${quotes.size}")
                    _myQuotes.value = quotes
                    // Extraer los IDs de solicitudes cotizadas
                    val quotedIds = quotes.map { it.requestId }.toSet()
                    _quotedRequestIds.value = quotedIds
                    Log.d(TAG, "💰 RequestIDs cotizados: $quotedIds")
                }
        }
    }

    /**
     * Carga todos los datos necesarios (requests, quotes, accepted quotes) antes de mostrar la UI
     * ⭐ Este método espera a que TODOS los endpoints terminen antes de mostrar la data
     */
    private fun loadAllData() {
        viewModelScope.launch {
            try {
                // ⭐ Marcar que la carga inicial está en progreso
                isInitialLoadInProgress = true
                _uiState.value = UiState.Loading
                
                val companyId = _companyId.value
                if (companyId == null) {
                    Log.e(TAG, "❌❌ No se pudo obtener el companyId - ES NULL")
                    _uiState.value = UiState.Error("No se pudo obtener el ID de la compañía")
                    isInitialLoadInProgress = false
                    return@launch
                }
                
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "🔄 CARGANDO TODOS LOS DATOS...")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                
                // 1. Cargar solicitudes
                Log.d(TAG, "📋 Paso 1: Cargando solicitudes...")
                inboxRepository.refreshIncomingRequests(companyId)
                Log.d(TAG, "✅ ✅ Solicitudes cargadas: ${incomingRequests.value.size} items")
                
                // 2. Cargar cotizaciones
                Log.d(TAG, "💰 Paso 2: Cargando cotizaciones...")
                quotesRepository.refreshQuotesByCompany(companyId)
                Log.d(TAG, "✅ ✅ Cotizaciones cargadas")
                
                // 3. Obtener quotes desde el repositorio para actualizar quotedRequestIds
                Log.d(TAG, "💰 Paso 2.1: Obteniendo quotes para actualizar quotedRequestIds...")
                val quotes = quotesRepository.observeQuotesByCompany(companyId).first()
                _myQuotes.value = quotes
                val quotedIds = quotes.map { it.requestId }.toSet()
                _quotedRequestIds.value = quotedIds
                Log.d(TAG, "✅ ✅ RequestIDs cotizados actualizados: $quotedIds")
                
                // 4. Cargar quotes aceptadas
                Log.d(TAG, "✅ Paso 3: Cargando quotes aceptadas...")
                val acceptedQuotes = quotesRepository.getAcceptedQuotesByCompany(companyId)
                _acceptedQuotesState.value = acceptedQuotes
                Log.d(TAG, "✅ ✅ Quotes aceptadas cargadas: ${acceptedQuotes.size} requests")
                Log.d(TAG, "   RequestIds con quotes aceptadas: ${acceptedQuotes.keys}")
                
                // 5. Finalmente, mostrar la UI
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "✅✅✅ TODOS LOS DATOS CARGADOS - MOSTRANDO UI")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                
                // ⭐ Marcar que la carga inicial terminó ANTES de actualizar el estado
                isInitialLoadInProgress = false
                _uiState.value = UiState.Success(incomingRequests.value)
                
            } catch (e: Exception) {
                Log.e(TAG, "❌❌❌ ERROR al cargar todos los datos", e)
                Log.e(TAG, "   Tipo: ${e::class.simpleName}")
                Log.e(TAG, "   Mensaje: ${e.message}")
                isInitialLoadInProgress = false
                _uiState.value = UiState.Error(e.message ?: "Error al cargar datos")
            }
        }
    }
    
    /**
     * Refresca TODOS los datos (requests, quotes, accepted quotes)
     * ⭐ Método público para refrescar completamente toda la información
     */
    fun refreshAllData() {
        Log.d(TAG, "🔄 refreshAllData() llamado - Refrescando TODA la información")
        loadAllData()
    }
    
    /**
     * Refresca las solicitudes desde el backend
     */
    fun refreshRequests() {
        refreshRequestsWithRetry(isFromWebSocket = false)
    }
    
    /**
     * ⭐ MEJORADO: Refresca las solicitudes con retry y delay opcional
     * @param isFromWebSocket Si es true, agrega delay y retry para evitar race conditions
     */
    private fun refreshRequestsWithRetry(isFromWebSocket: Boolean = false) {
        Log.d(TAG, "🔄 refreshRequestsWithRetry() llamado (isFromWebSocket=$isFromWebSocket)")
        viewModelScope.launch {
            try {
                // ⭐ MEJORADO: Delay si viene de WebSocket para dar tiempo al backend
                if (isFromWebSocket) {
                    Log.d(TAG, "⏳ Esperando 1 segundo antes de refrescar (evitar race condition)...")
                    delay(1000) // 1 segundo de delay
                }
                
                // ⭐ NO cambiar el estado si la carga inicial está en progreso
                if (!isInitialLoadInProgress) {
                    Log.d(TAG, "🔄 Seteando UI a Loading...")
                    _uiState.value = UiState.Loading
                } else {
                    Log.d(TAG, "⏳ Carga inicial en progreso - No cambiar estado a Loading")
                }
                _refreshErrorAfterNotification.value = null // Limpiar error anterior

                val companyId = _companyId.value
                Log.d(TAG, "🔑 CompanyId actual: $companyId")

                if (companyId != null) {
                    Log.d(TAG, "✅ CompanyId válido, llamando a repository.refreshIncomingRequests($companyId)...")

                    inboxRepository.refreshIncomingRequests(companyId)

                    Log.d(TAG, "✅ Repository refresh completado")
                    Log.d(TAG, "📊 incomingRequests.value tiene: ${incomingRequests.value.size} items")

                    // ⭐ Solo actualizar el estado si la carga inicial NO está en progreso
                    if (!isInitialLoadInProgress) {
                        _uiState.value = UiState.Success(incomingRequests.value)
                        Log.d(TAG, "✅✅ Solicitudes refrescadas correctamente: ${incomingRequests.value.size} items")
                    } else {
                        Log.d(TAG, "⏳ Carga inicial en progreso - No actualizar estado todavía")
                    }
                } else {
                    Log.e(TAG, "❌❌ No se pudo obtener el companyId - ES NULL")
                    val errorMsg = "No se pudo obtener el ID de la compañía"
                    if (!isInitialLoadInProgress) {
                        _uiState.value = UiState.Error(errorMsg)
                    }
                    if (isFromWebSocket) {
                        _refreshErrorAfterNotification.value = errorMsg
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌❌ ERROR al refrescar solicitudes", e)
                Log.e(TAG, "   Tipo: ${e::class.simpleName}")
                Log.e(TAG, "   Mensaje: ${e.message}")
                Log.e(TAG, "   Stack:", e)
                
                val errorMsg = e.message ?: "Error al cargar solicitudes"
                if (!isInitialLoadInProgress) {
                    _uiState.value = UiState.Error(errorMsg)
                }
                
                // ⭐ MEJORADO: Si viene de WebSocket y falla, intentar retry
                if (isFromWebSocket) {
                    _refreshErrorAfterNotification.value = errorMsg
                    
                    // ⭐ MEJORADO: Retry después de 2 segundos (solo una vez)
                    Log.d(TAG, "🔄 Intentando retry en 2 segundos...")
                    delay(2000)
                    
                    try {
                        val companyId = _companyId.value
                        if (companyId != null) {
                            Log.d(TAG, "🔄 Retry: Refrescando solicitudes...")
                            inboxRepository.refreshIncomingRequests(companyId)
                            if (!isInitialLoadInProgress) {
                                _uiState.value = UiState.Success(incomingRequests.value)
                            }
                            _refreshErrorAfterNotification.value = null // Limpiar error si el retry funciona
                            Log.d(TAG, "✅✅ Retry exitoso - Solicitudes refrescadas")
                        }
                    } catch (retryException: Exception) {
                        Log.e(TAG, "❌❌ Retry también falló", retryException)
                        _refreshErrorAfterNotification.value = "Error al actualizar después de nueva solicitud. Por favor, recarga manualmente."
                    }
                }
            }
        }
    }

    /**
     * Carga los detalles completos de una solicitud
     */
    fun loadRequestDetails(requestId: Long) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Cargando detalles de request $requestId")
                _detailState.value = DetailState.Loading
                val request = inboxRepository.getRequestDetail(requestId)
                _detailState.value = DetailState.Success(request)
                Log.d(TAG, "✅ Detalles cargados para request $requestId")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al cargar detalles de request $requestId", e)
                _detailState.value = DetailState.Error(
                    e.message ?: "Error al cargar detalles"
                )
            }
        }
    }

    /**
     * Cierra el modal de detalles
     */
    fun closeDetails() {
        _detailState.value = DetailState.Idle
    }
    
    /**
     * Refresca las cotizaciones de la compañía
     */
    fun refreshQuotes() {
        Log.d(TAG, "💰 refreshQuotes() llamado")
        viewModelScope.launch {
            try {
                val companyId = _companyId.value
                if (companyId != null) {
                    Log.d(TAG, "💰 Refrescando cotizaciones para companyId=$companyId...")
                    quotesRepository.refreshQuotesByCompany(companyId)
                    Log.d(TAG, "💰 ✅ Cotizaciones refrescadas")
                    // También refrescar quotes aceptadas
                    loadAcceptedQuotes()
                } else {
                    Log.w(TAG, "💰 ⚠️ No se puede refrescar - companyId es null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "💰 ❌ Error al refrescar cotizaciones", e)
                // No bloqueamos la UI por error en cotizaciones
            }
        }
    }
    
    /**
     * Carga las quotes aceptadas (TRATO, ACEPTADA, CERRADA) con sus estados
     */
    fun loadAcceptedQuotes() {
        Log.d(TAG, "✅ loadAcceptedQuotes() llamado")
        viewModelScope.launch {
            try {
                val companyId = _companyId.value
                if (companyId != null) {
                    Log.d(TAG, "✅ Cargando quotes aceptadas para companyId=$companyId...")
                    val acceptedQuotes = quotesRepository.getAcceptedQuotesByCompany(companyId)
                    _acceptedQuotesState.value = acceptedQuotes
                    Log.d(TAG, "✅ ✅ Quotes aceptadas cargadas: ${acceptedQuotes.size} requests")
                    Log.d(TAG, "   RequestIds: ${acceptedQuotes.keys}")
                } else {
                    Log.w(TAG, "✅ ⚠️ No se puede cargar - companyId es null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "✅ ❌ Error al cargar quotes aceptadas", e)
                // No bloqueamos la UI por error
            }
        }
    }

    /**
     * Elimina una solicitud del inbox (TODO: implementar endpoint si existe)
     */
    fun deleteRequest(requestId: Long) {
        viewModelScope.launch {
            // TODO: Implementar cuando exista el endpoint de eliminación
            Log.d(TAG, "TODO: Eliminar request $requestId")
        }
    }

    /**
     * Descarta la notificación de nueva solicitud manualmente
     */
    fun dismissNotification() {
        _newRequestNotification.value = null
        Log.d(TAG, "👋 Notificación descartada manualmente")
    }

    /**
     * ⭐ MEJORADO: Parsea el requestId del mensaje WebSocket usando JSONObject
     * El mensaje viene en formato JSON: {"type":"NEW_REQUEST","requestId": 123, "companyId": 52, ...}
     */
    private fun parseRequestIdFromWebSocketMessage(content: String?): Long? {
        if (content.isNullOrBlank()) return null
        
        return try {
            // ⭐ MEJORADO: Intentar parsear como JSON primero
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

    // ========== HELPERS PARA FILTRADO ==========
    
    /**
     * Verifica si una solicitud ya fue cotizada
     */
    fun isRequestQuoted(requestId: Long): Boolean {
        return requestId in _quotedRequestIds.value
    }
    
    /**
     * Obtiene el quoteId de una solicitud específica (si existe)
     */
    fun getQuoteIdForRequest(requestId: Long): Long? {
        return _myQuotes.value.find { it.requestId == requestId }?.quoteId
    }
    
    /**
     * Obtiene todas las solicitudes (cotizadas y no cotizadas)
     */
    fun getAllRequests(): List<IncomingRequestSummary> {
        return incomingRequests.value
    }
    
    /**
     * Obtiene solo las solicitudes ABIERTAS (no cotizadas)
     */
    fun getOpenRequests(): List<IncomingRequestSummary> {
        return incomingRequests.value.filter { !isRequestQuoted(it.requestId) }
    }
    
    /**
     * Obtiene solo las solicitudes EN PROCESO (ya cotizadas pero NO aceptadas)
     */
    fun getInProgressRequests(): List<IncomingRequestSummary> {
        val acceptedRequestIds = _acceptedQuotesState.value.keys
        return incomingRequests.value.filter { 
            isRequestQuoted(it.requestId) && it.requestId !in acceptedRequestIds
        }
    }
    
    /**
     * Obtiene las solicitudes con cotizaciones aceptadas (TRATO, ACEPTADA, CERRADA)
     */
    fun getAcceptedRequests(): List<IncomingRequestSummary> {
        val acceptedRequestIds = _acceptedQuotesState.value.keys
        return incomingRequests.value.filter { it.requestId in acceptedRequestIds }
    }
    
    /**
     * Obtiene el estado de una cotización aceptada para un requestId
     */
    fun getAcceptedQuoteState(requestId: Long): String? {
        return _acceptedQuotesState.value[requestId]
    }

    // Estadísticas
    fun getTotalRequests(): Int = incomingRequests.value.size

    fun getOpenRequestsCount(): Int = getOpenRequests().size
    
    fun getInProgressCount(): Int = getInProgressRequests().size
    
    fun getAcceptedCount(): Int = getAcceptedRequests().size
}

