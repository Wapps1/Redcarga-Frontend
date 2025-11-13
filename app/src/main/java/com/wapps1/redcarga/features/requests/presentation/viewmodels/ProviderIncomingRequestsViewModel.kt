package com.wapps1.redcarga.features.requests.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wapps1.redcarga.core.session.AuthSessionStore
import com.wapps1.redcarga.core.websocket.RedcargaWebSocketManager
import com.wapps1.redcarga.core.websocket.WebSocketMessageType
import com.wapps1.redcarga.features.requests.domain.models.IncomingRequestSummary
import com.wapps1.redcarga.features.requests.domain.models.Request
import com.wapps1.redcarga.features.requests.domain.repositories.PlanningInboxRepository
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

    init {
        Log.d(TAG, "🎬 ViewModel inicializado")

        // Observar cambios en las solicitudes para actualizar el UI state
        viewModelScope.launch {
            Log.d(TAG, "🔄 Iniciando observación de incomingRequests...")
            incomingRequests.collect { requests ->
                Log.d(TAG, "📥 incomingRequests emitió: ${requests.size} solicitudes")
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
                    refreshRequests()
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
                } else if (companyId == null) {
                    Log.w(TAG, "⚠️ currentCompanyId es null")
                }
                lastCompanyId = companyId
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
     * Elimina una solicitud del inbox (TODO: implementar endpoint si existe)
     */
    fun deleteRequest(requestId: Long) {
        viewModelScope.launch {
            // TODO: Implementar cuando exista el endpoint de eliminación
            Log.d(TAG, "TODO: Eliminar request $requestId")
        }
    }

    // Estadísticas
    fun getTotalRequests(): Int = incomingRequests.value.size

    fun getOpenRequests(): Int = incomingRequests.value.count { it.isOpen() }
}

