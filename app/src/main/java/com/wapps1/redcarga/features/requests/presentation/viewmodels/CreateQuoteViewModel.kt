package com.wapps1.redcarga.features.requests.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wapps1.redcarga.core.session.AuthSessionStore
import com.wapps1.redcarga.features.requests.domain.models.CreateQuoteRequest
import com.wapps1.redcarga.features.requests.domain.models.QuoteItemRequest
import com.wapps1.redcarga.features.requests.domain.models.Request
import com.wapps1.redcarga.features.requests.domain.models.RequestItem
import com.wapps1.redcarga.features.requests.domain.repositories.PlanningInboxRepository
import com.wapps1.redcarga.features.requests.domain.repositories.QuotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

private const val TAG = "CreateQuoteViewModel"

@HiltViewModel
class CreateQuoteViewModel @Inject constructor(
    private val quotesRepository: QuotesRepository,
    private val planningInboxRepository: PlanningInboxRepository,
    private val authSessionStore: AuthSessionStore
) : ViewModel() {

    sealed class UiState {
        data object Idle : UiState()
        data object Loading : UiState()
        data class Success(val request: Request) : UiState()
        data class Error(val message: String) : UiState()
    }

    sealed class SubmitState {
        data object Idle : SubmitState()
        data object Loading : SubmitState()
        data class Success(val quoteId: Long) : SubmitState()
        data class Error(val message: String) : SubmitState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    // Mapa de requestItemId -> cantidad que el proveedor cotizará
    private val _itemQuantities = MutableStateFlow<Map<Long, BigDecimal>>(emptyMap())
    val itemQuantities: StateFlow<Map<Long, BigDecimal>> = _itemQuantities.asStateFlow()

    // Precio total de la cotización
    private val _totalAmount = MutableStateFlow(BigDecimal.ZERO)
    val totalAmount: StateFlow<BigDecimal> = _totalAmount.asStateFlow()

    private var currentRequest: Request? = null

    /**
     * Carga los detalles de una solicitud
     */
    fun loadRequestDetails(requestId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                Log.d(TAG, "📥 Cargando detalles de solicitud $requestId")

                val request = planningInboxRepository.getRequestDetail(requestId)
                currentRequest = request

                // Inicializar cantidades con el total de cada item
                val initialQuantities = request.items.associate { item ->
                    item.itemId!! to item.quantity.toBigDecimal()
                }
                _itemQuantities.value = initialQuantities

                _uiState.value = UiState.Success(request)
                Log.d(TAG, "✅ Solicitud cargada: ${request.requestName}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al cargar solicitud", e)
                _uiState.value = UiState.Error(e.message ?: "Error al cargar los detalles")
            }
        }
    }

    /**
     * Actualiza el precio total de la cotización
     */
    fun updateTotalAmount(amount: BigDecimal) {
        _totalAmount.value = amount
        Log.d(TAG, "💰 Precio actualizado: $amount")
    }

    /**
     * Actualiza la cantidad a cotizar de un item específico
     */
    fun updateItemQuantity(itemId: Long, quantity: BigDecimal) {
        _itemQuantities.value = _itemQuantities.value.toMutableMap().apply {
            this[itemId] = quantity
        }
        Log.d(TAG, "📦 Cantidad actualizada para item $itemId: $quantity")
    }

    /**
     * Valida y envía la cotización
     */
    fun submitQuote() {
        viewModelScope.launch {
            try {
                _submitState.value = SubmitState.Loading

                val request = currentRequest
                if (request == null) {
                    _submitState.value = SubmitState.Error("No hay solicitud cargada")
                    return@launch
                }

                val companyId = authSessionStore.currentCompanyId.value
                if (companyId == null) {
                    _submitState.value = SubmitState.Error("No se pudo obtener el ID de la compañía")
                    return@launch
                }

                // Validar precio total
                if (_totalAmount.value <= BigDecimal.ZERO) {
                    _submitState.value = SubmitState.Error("El precio total debe ser mayor a 0")
                    return@launch
                }

                // Construir items de la cotización
                val quoteItems = _itemQuantities.value.mapNotNull { (itemId, qty) ->
                    if (qty > BigDecimal.ZERO) {
                        QuoteItemRequest(
                            requestItemId = itemId,
                            qty = qty
                        )
                    } else null
                }

                if (quoteItems.isEmpty()) {
                    _submitState.value = SubmitState.Error("Debe cotizar al menos un item")
                    return@launch
                }

                Log.d(TAG, "📤 Enviando cotización:")
                Log.d(TAG, "   requestId: ${request.requestId}")
                Log.d(TAG, "   companyId: $companyId")
                Log.d(TAG, "   totalAmount: ${_totalAmount.value}")
                Log.d(TAG, "   items: ${quoteItems.size}")

                // Crear la cotización
                val createQuoteRequest = CreateQuoteRequest(
                    requestId = request.requestId,
                    companyId = companyId,
                    totalAmount = _totalAmount.value,
                    currency = "PEN",
                    items = quoteItems
                )

                val response = quotesRepository.createQuote(createQuoteRequest)

                Log.d(TAG, "✅ Cotización creada exitosamente: quoteId=${response.quoteId}")
                _submitState.value = SubmitState.Success(response.quoteId)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al enviar cotización", e)
                val errorMessage = when {
                    e.message?.contains("401") == true -> "No autorizado. Por favor, inicia sesión nuevamente"
                    e.message?.contains("400") == true -> "Datos inválidos. Verifica la información"
                    e.message?.contains("500") == true -> "Error del servidor. Intenta más tarde"
                    e.message?.contains("timeout") == true -> "Tiempo de espera agotado. Verifica tu conexión"
                    else -> e.message ?: "Error desconocido al crear la cotización"
                }
                _submitState.value = SubmitState.Error(errorMessage)
            }
        }
    }

    /**
     * Resetea el estado de envío
     */
    fun resetSubmitState() {
        _submitState.value = SubmitState.Idle
    }

    /**
     * Resetea todo el formulario
     */
    fun resetForm() {
        _uiState.value = UiState.Idle
        _submitState.value = SubmitState.Idle
        _itemQuantities.value = emptyMap()
        _totalAmount.value = BigDecimal.ZERO
        currentRequest = null
    }
}

