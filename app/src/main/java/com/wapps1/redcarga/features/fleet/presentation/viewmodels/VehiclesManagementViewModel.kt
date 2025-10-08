package com.wapps1.redcarga.features.fleet.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wapps1.redcarga.core.session.AuthSessionStore
import com.wapps1.redcarga.features.fleet.domain.models.common.CompanyId
import com.wapps1.redcarga.features.fleet.domain.models.common.VehicleId
import com.wapps1.redcarga.features.fleet.domain.models.vehicles.Vehicle
import com.wapps1.redcarga.features.fleet.domain.models.vehicles.VehicleUpsert
import com.wapps1.redcarga.features.fleet.domain.repositories.FleetVehiclesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

 

@HiltViewModel
class VehiclesManagementViewModel @Inject constructor(
    private val repo: FleetVehiclesRepository,
    private val sessionStore: AuthSessionStore
) : ViewModel() {

    data class VehicleItemUi(
        val id: Long,
        val name: String,
        val plate: String,
        val active: Boolean
    )

    data class Filters(
        val activeOnly: Boolean? = null,
        val query: String? = null
    )

    data class UiState(
        val isInitializing: Boolean = true,
        val isRefreshing: Boolean = false,
        val isSubmitting: Boolean = false,
        val companyId: Long? = null,
        val all: List<VehicleItemUi> = emptyList(),
        val items: List<VehicleItemUi> = emptyList(),
        val filters: Filters = Filters(),
        val empty: Boolean = false
    ) {
        val hasActiveFilters: Boolean
            get() = filters.activeOnly != null || !filters.query.isNullOrBlank()

        val activeFiltersCount: Int
            get() = listOfNotNull(
                filters.activeOnly,
                filters.query?.takeIf { it.isNotBlank() }
            ).size
    }

    sealed interface Effect { data class Message(val text: String): Effect }

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()
    private val _effects = Channel<Effect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun bootstrap() {
        if (!_state.value.isInitializing) return
        viewModelScope.launch {
            sessionStore.currentCompanyId.collectLatest { cid ->
                if (cid == null) {
                    _state.value = _state.value.copy(isInitializing = false, companyId = null, items = emptyList(), empty = true)
                    return@collectLatest
                }
                _state.value = _state.value.copy(companyId = cid)
                repo.observeVehicles(CompanyId(cid)).collectLatest { list ->
                    val ui = list.map { it.toUi() }
                    val filtered = applyFilters(ui, _state.value.filters)
                    _state.value = _state.value.copy(isInitializing = false, all = ui, items = filtered, empty = filtered.isEmpty())
                }
            }
        }
    }

    private fun Vehicle.toUi() = VehicleItemUi(
        id = vehicleId.value,
        name = name,
        plate = plate,
        active = active
    )

    private fun applyFilters(items: List<VehicleItemUi>, f: Filters): List<VehicleItemUi> {
        val q = f.query?.trim()?.lowercase().orEmpty()
        return items.asSequence()
            .filter { f.activeOnly == null || it.active == f.activeOnly }
            .filter { if (q.isEmpty()) true else it.name.lowercase().contains(q) || it.plate.lowercase().contains(q) }
            .toList()
    }

    fun onFiltersChanged(filters: Filters) {
        val filtered = applyFilters(_state.value.all, filters)
        _state.value = _state.value.copy(filters = filters, items = filtered, empty = filtered.isEmpty())
    }

    fun onRefresh() {
        val cid = _state.value.companyId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            runCatching { repo.refreshVehicles(CompanyId(cid)) }
                .onFailure { _effects.send(Effect.Message(it.message ?: "Error al actualizar")) }
            _state.value = _state.value.copy(isRefreshing = false)
        }
    }

    fun onCreate(name: String, plate: String, active: Boolean) {
        val cid = _state.value.companyId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            runCatching { repo.createVehicle(CompanyId(cid), VehicleUpsert(name, plate, active)) }
                .onSuccess { _effects.send(Effect.Message("Vehículo creado")) }
                .onFailure { _effects.send(Effect.Message(it.message ?: "Error al crear vehículo")) }
            _state.value = _state.value.copy(isSubmitting = false)
        }
    }

    fun onUpdate(vehicleId: Long, name: String, plate: String, active: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            runCatching { repo.updateVehicle(VehicleId(vehicleId), VehicleUpsert(name, plate, active)) }
                .onSuccess { _effects.send(Effect.Message("Vehículo actualizado")) }
                .onFailure { _effects.send(Effect.Message(it.message ?: "Error al actualizar vehículo")) }
            _state.value = _state.value.copy(isSubmitting = false)
        }
    }

    fun onDelete(vehicleId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            runCatching { repo.deleteVehicle(VehicleId(vehicleId)) }
                .onSuccess { _effects.send(Effect.Message("Vehículo eliminado")) }
                .onFailure { _effects.send(Effect.Message(it.message ?: "Error al eliminar vehículo")) }
            _state.value = _state.value.copy(isSubmitting = false)
        }
    }
}


