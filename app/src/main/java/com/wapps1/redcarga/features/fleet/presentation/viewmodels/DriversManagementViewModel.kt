package com.wapps1.redcarga.features.fleet.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wapps1.redcarga.core.session.AuthSessionStore
import com.wapps1.redcarga.features.fleet.domain.models.common.CompanyId
import com.wapps1.redcarga.features.fleet.domain.models.common.DriverId
import com.wapps1.redcarga.features.fleet.domain.models.drivers.Driver
import com.wapps1.redcarga.features.fleet.domain.models.drivers.DriverUpsert
import com.wapps1.redcarga.features.fleet.domain.repositories.FleetDriversRepository
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
class DriversManagementViewModel @Inject constructor(
    private val repo: FleetDriversRepository,
    private val sessionStore: AuthSessionStore
) : ViewModel() {

    data class DriverItemUi(
        val id: Long,
        val firstName: String,
        val lastName: String,
        val email: String,
        val phone: String,
        val licenseNumber: String,
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
        val all: List<DriverItemUi> = emptyList(),
        val items: List<DriverItemUi> = emptyList(),
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
                repo.observeDrivers(CompanyId(cid)).collectLatest { list ->
                    val ui = list.map { it.toUi() }
                    val filtered = applyFilters(ui, _state.value.filters)
                    _state.value = _state.value.copy(isInitializing = false, all = ui, items = filtered, empty = filtered.isEmpty())
                }
            }
        }
    }

    private fun Driver.toUi() = DriverItemUi(
        id = driverId.value,
        firstName = firstName,
        lastName = lastName,
        email = email.value,
        phone = phone,
        licenseNumber = licenseNumber,
        active = active
    )

    private fun applyFilters(items: List<DriverItemUi>, f: Filters): List<DriverItemUi> {
        val q = f.query?.trim()?.lowercase().orEmpty()
        return items.asSequence()
            .filter { f.activeOnly == null || it.active == f.activeOnly }
            .filter {
                if (q.isEmpty()) true else
                    it.firstName.lowercase().contains(q) ||
                            it.lastName.lowercase().contains(q) ||
                            it.email.lowercase().contains(q) ||
                            it.phone.lowercase().contains(q) ||
                            it.licenseNumber.lowercase().contains(q)
            }
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
            runCatching { repo.refreshDrivers(CompanyId(cid)) }
                .onFailure { _effects.send(Effect.Message(it.message ?: "Error al actualizar")) }
            _state.value = _state.value.copy(isRefreshing = false)
        }
    }

    fun onCreate(firstName: String, lastName: String, email: String, phone: String, license: String, active: Boolean) {
        val cid = _state.value.companyId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            runCatching { repo.createDriver(CompanyId(cid), DriverUpsert(firstName, lastName, com.wapps1.redcarga.features.auth.domain.models.value.Email(email), phone, license, active)) }
                .onSuccess { _effects.send(Effect.Message("Conductor creado")) }
                .onFailure { _effects.send(Effect.Message(it.message ?: "Error al crear conductor")) }
            _state.value = _state.value.copy(isSubmitting = false)
        }
    }

    fun onUpdate(driverId: Long, firstName: String, lastName: String, email: String, phone: String, license: String, active: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            runCatching { repo.updateDriver(DriverId(driverId), DriverUpsert(firstName, lastName, com.wapps1.redcarga.features.auth.domain.models.value.Email(email), phone, license, active)) }
                .onSuccess { _effects.send(Effect.Message("Conductor actualizado")) }
                .onFailure { _effects.send(Effect.Message(it.message ?: "Error al actualizar conductor")) }
            _state.value = _state.value.copy(isSubmitting = false)
        }
    }

    fun onDelete(driverId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            runCatching { repo.deleteDriver(DriverId(driverId)) }
                .onSuccess { _effects.send(Effect.Message("Conductor eliminado")) }
                .onFailure { _effects.send(Effect.Message(it.message ?: "Error al eliminar conductor")) }
            _state.value = _state.value.copy(isSubmitting = false)
        }
    }
}


