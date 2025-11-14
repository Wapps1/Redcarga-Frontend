package com.wapps1.redcarga.features.deals.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wapps1.redcarga.features.requests.domain.models.QuoteDetail
import com.wapps1.redcarga.features.requests.domain.models.RequestSummary
import com.wapps1.redcarga.features.requests.domain.repositories.QuotesRepository
import com.wapps1.redcarga.features.requests.domain.repositories.RequestsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ClientDealsVM"

@HiltViewModel
class ClientDealsViewModel @Inject constructor(
    private val requestsRepository: RequestsRepository,
    private val quotesRepository: QuotesRepository
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

    init {
        // Cargar solicitudes al iniciar
        refreshRequests()
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

                Log.d(TAG, "✅✅ Cotizaciones cargadas: ${quotes.size} items para requestId=$requestId, state=$state")

                // Guardar en el mapa con clave que incluye el estado
                val quotesKey = "${requestId}_$stateKey"
                _quotesByRequestId.value = _quotesByRequestId.value.toMutableMap().apply {
                    put(quotesKey, quotes)
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
}


