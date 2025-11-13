package com.wapps1.redcarga.features.requests.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wapps1.redcarga.features.requests.domain.models.Request
import com.wapps1.redcarga.features.requests.domain.models.RequestSummary
import com.wapps1.redcarga.features.requests.domain.repositories.RequestsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientRequestsViewModel @Inject constructor(
    private val requestsRepository: RequestsRepository
) : ViewModel() {

    // Estados de la UI
    sealed class UiState {
        object Loading : UiState()
        data class Success(val requests: List<RequestSummary>) : UiState()
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

    // Observar solicitudes del cliente desde el repositorio
    val clientRequests: StateFlow<List<RequestSummary>> = requestsRepository
        .observeClientRequests()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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

    // Cargar detalles de una solicitud específica
    fun loadRequestDetails(requestId: Long) {
        viewModelScope.launch {
            try {
                _detailState.value = DetailState.Loading
                val request = requestsRepository.getRequestById(requestId)
                _detailState.value = DetailState.Success(request)
            } catch (e: Exception) {
                _detailState.value = DetailState.Error(
                    e.message ?: "Error al cargar los detalles"
                )
            }
        }
    }

    // Cerrar el modal de detalles
    fun closeDetails() {
        _detailState.value = DetailState.Idle
    }

    // Obtener estadísticas
    fun getTotalRequests(): Int = clientRequests.value.size

    fun getActiveRequests(): Int = clientRequests.value.count { it.isOpen() }

    fun getCompletedRequests(): Int = clientRequests.value.count {
        it.status == com.wapps1.redcarga.features.requests.domain.models.RequestStatus.COMPLETED
    }
}

