package com.wapps1.redcarga.features.requests.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wapps1.redcarga.features.requests.domain.models.QuoteDetail
import com.wapps1.redcarga.features.requests.domain.repositories.QuotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ViewQuoteVM"

/**
 * ViewModel para ver los detalles de una cotización existente
 */
@HiltViewModel
class ViewQuoteViewModel @Inject constructor(
    private val quotesRepository: QuotesRepository
) : ViewModel() {

    // Estados de la UI
    sealed class UiState {
        object Loading : UiState()
        data class Success(val quote: QuoteDetail) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * Carga los detalles de la cotización
     */
    fun loadQuoteDetails(quoteId: Long) {
        Log.d(TAG, "💰 Cargando detalles de cotización $quoteId")
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                Log.d(TAG, "💰 Obteniendo detalles desde repositorio...")
                
                val quoteDetail = quotesRepository.getQuoteDetail(quoteId)
                
                Log.d(TAG, "💰 ✅ Cotización cargada:")
                Log.d(TAG, "   quoteId: ${quoteDetail.quoteId}")
                Log.d(TAG, "   requestId: ${quoteDetail.requestId}")
                Log.d(TAG, "   companyId: ${quoteDetail.companyId}")
                Log.d(TAG, "   totalAmount: ${quoteDetail.totalAmount} ${quoteDetail.currencyCode}")
                Log.d(TAG, "   stateCode: ${quoteDetail.stateCode}")
                Log.d(TAG, "   items: ${quoteDetail.items.size}")
                
                _uiState.value = UiState.Success(quoteDetail)
            } catch (e: Exception) {
                Log.e(TAG, "💰 ❌ Error al cargar cotización", e)
                _uiState.value = UiState.Error(
                    e.message ?: "Error al cargar la cotización"
                )
            }
        }
    }
}

