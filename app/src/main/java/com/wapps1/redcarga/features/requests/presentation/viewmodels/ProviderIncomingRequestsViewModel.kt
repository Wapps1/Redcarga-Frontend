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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
                    _uiState.value = UiState.Success(requests)
                    Log.d(TAG, "✅ UI actualizado a Success con ${requests.size} items (companyId=$currentCompanyId)")
                } else {
                    Log.w(TAG, "⚠️ No se actualiza UI porque companyId sigue siendo null")
                }
            }
        }

        // Observar mensajes WebSocket para auto-refresh
        viewModelScope.launch {
            Log.d(TAG, "🔌 Iniciando observación de WebSocket messages...")
            webSocketManager.receivedMessages.collect { messages ->
                val lastMessage = messages.lastOrNull()
                if (lastMessage?.type == WebSocketMessageType.NEW_REQUEST) {
                    Log.d(TAG, "🔔 Nueva solicitud detectada via WebSocket!")
                    Log.d(TAG, "📨 Contenido: ${lastMessage.content}")
                    
                    // Parsear el requestId del mensaje si está disponible
                    val requestId = parseRequestIdFromWebSocketMessage(lastMessage.content)
                    if (requestId != null) {
                        _lastNewRequestId.value = requestId
                        Log.d(TAG, "🆕 Marcando request $requestId como nueva")
                        
                        // Auto-limpiar después de 15 segundos
                        launch {
                            kotlinx.coroutines.delay(15000)
                            if (_lastNewRequestId.value == requestId) {
                                _lastNewRequestId.value = null
                                Log.d(TAG, "⏰ Limpiando marca de nueva solicitud")
                            }
                        }
                    }
                    
                    // Mostrar notificación
                    _newRequestNotification.value = "¡Nueva solicitud recibida! 🎉"
                    
                    // Refrescar la lista
                    refreshRequests()
                    
                    // Auto-ocultar notificación después de 5 segundos
                    launch {
                        kotlinx.coroutines.delay(5000)
                        _newRequestNotification.value = null
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
                    refreshRequests()
                    refreshQuotes() // ⭐ También refrescar cotizaciones
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
     * Refresca las solicitudes desde el backend
     */
    fun refreshRequests() {
        Log.d(TAG, "🔄 refreshRequests() llamado")
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔄 Seteando UI a Loading...")
                _uiState.value = UiState.Loading

                val companyId = _companyId.value
                Log.d(TAG, "🔑 CompanyId actual: $companyId")

                if (companyId != null) {
                    Log.d(TAG, "✅ CompanyId válido, llamando a repository.refreshIncomingRequests($companyId)...")

                    inboxRepository.refreshIncomingRequests(companyId)

                    Log.d(TAG, "✅ Repository refresh completado")
                    Log.d(TAG, "📊 incomingRequests.value tiene: ${incomingRequests.value.size} items")

                    _uiState.value = UiState.Success(incomingRequests.value)
                    Log.d(TAG, "✅✅ Solicitudes refrescadas correctamente: ${incomingRequests.value.size} items")
                } else {
                    Log.e(TAG, "❌❌ No se pudo obtener el companyId - ES NULL")
                    _uiState.value = UiState.Error("No se pudo obtener el ID de la compañía")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌❌ ERROR al refrescar solicitudes", e)
                Log.e(TAG, "   Tipo: ${e::class.simpleName}")
                Log.e(TAG, "   Mensaje: ${e.message}")
                Log.e(TAG, "   Stack:", e)
                _uiState.value = UiState.Error(
                    e.message ?: "Error al cargar solicitudes"
                )
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
     * Parsea el requestId del mensaje WebSocket
     * El mensaje viene en formato JSON: {"requestId": 123, "companyId": 52, ...}
     */
    private fun parseRequestIdFromWebSocketMessage(content: String?): Long? {
        if (content.isNullOrBlank()) return null
        
        return try {
            // Buscar "requestId": seguido de un número
            val regex = """"requestId"\s*:\s*(\d+)""".toRegex()
            val matchResult = regex.find(content)
            matchResult?.groupValues?.get(1)?.toLongOrNull()
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ No se pudo parsear requestId del mensaje WebSocket: ${e.message}")
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
     * Obtiene solo las solicitudes EN PROCESO (ya cotizadas)
     */
    fun getInProgressRequests(): List<IncomingRequestSummary> {
        return incomingRequests.value.filter { isRequestQuoted(it.requestId) }
    }
    
    // Estadísticas
    fun getTotalRequests(): Int = incomingRequests.value.size

    fun getOpenRequestsCount(): Int = getOpenRequests().size
    
    fun getInProgressCount(): Int = getInProgressRequests().size
}

