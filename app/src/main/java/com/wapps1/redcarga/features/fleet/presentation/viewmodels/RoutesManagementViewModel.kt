package com.wapps1.redcarga.features.fleet.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.wapps1.redcarga.core.session.AuthSessionStore
import com.wapps1.redcarga.core.session.UserType
import com.wapps1.redcarga.features.fleet.domain.models.common.CompanyId
import com.wapps1.redcarga.features.fleet.domain.models.common.RouteId
import com.wapps1.redcarga.features.fleet.domain.models.routes.Route
import com.wapps1.redcarga.features.fleet.domain.models.routes.RouteCreate
import com.wapps1.redcarga.features.fleet.domain.models.routes.RouteType
import com.wapps1.redcarga.features.fleet.domain.models.routes.RouteUpdate
import com.wapps1.redcarga.features.fleet.domain.repositories.GeoRepository
import com.wapps1.redcarga.features.fleet.domain.repositories.PlanningRoutesRepository

 

@HiltViewModel
class RoutesManagementViewModel @Inject constructor(
    private val planningRoutesRepository: PlanningRoutesRepository,
    private val geoRepository: GeoRepository,
    private val sessionStore: AuthSessionStore
) : ViewModel() {

    data class RouteItemUi(
        val id: Long,
        val type: RouteType,
        val originDeptCode: String,
        val originDeptName: String,
        val originProvCode: String?,
        val originProvName: String?,
        val destDeptCode: String,
        val destDeptName: String,
        val destProvCode: String?,
        val destProvName: String?,
        val active: Boolean
    )

    data class RouteFilters(
        val type: RouteType? = null,
        val activeOnly: Boolean? = null,
        val originDeptCode: String? = null,
        val originProvCode: String? = null,
        val destDeptCode: String? = null,
        val destProvCode: String? = null,
        val query: String? = null
    )

    data class UiState(
        val isInitializing: Boolean = true,
        val isRefreshing: Boolean = false,
        val isSubmitting: Boolean = false,
        val companyId: Long? = null,
        val allRoutes: List<RouteItemUi> = emptyList(),
        val routes: List<RouteItemUi> = emptyList(),
        val filters: RouteFilters = RouteFilters(),
        val error: String? = null,
        val empty: Boolean = false,
        // Catálogo para selects
        val departments: List<DeptOption> = emptyList(),
        val provincesByDept: Map<String, List<ProvOption>> = emptyMap(),
        val geoReady: Boolean = false
    ) {
        val hasActiveFilters: Boolean
            get() = filters.type != null || 
                    filters.activeOnly != null || 
                    filters.originDeptCode != null || 
                    filters.originProvCode != null ||
                    filters.destDeptCode != null ||
                    filters.destProvCode != null ||
                    !filters.query.isNullOrBlank()
        
        val activeFiltersCount: Int
            get() = listOfNotNull(
                filters.type,
                filters.activeOnly,
                filters.originDeptCode,
                filters.originProvCode,
                filters.destDeptCode,
                filters.destProvCode,
                filters.query?.takeIf { it.isNotBlank() }
            ).size
    }

    sealed interface Effect {
        data class Message(val text: String) : Effect
    }

    data class DeptOption(val code: String, val name: String)
    data class ProvOption(val code: String, val deptCode: String, val name: String)

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _effects = Channel<Effect>(capacity = Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun bootstrap() {
        if (!_state.value.isInitializing) return
        viewModelScope.launch {
            // Intentar refrescar catálogo geo si hace falta (no bloquea)
            launch {
                runCatching { geoRepository.refreshCatalog(force = false) }
            }

            // Observar companyId y, cuando esté, observar rutas de Room y componer nombres con GeoCatalog
            sessionStore.currentCompanyId.collectLatest { cid ->
                if (cid == null) {
                    _state.value = _state.value.copy(isInitializing = false, companyId = null, routes = emptyList(), empty = true)
                    return@collectLatest
                }

                _state.value = _state.value.copy(companyId = cid)

                val companyFlow = planningRoutesRepository.observeRoutesByCompany(CompanyId(cid))
                val catalogFlow = geoRepository.observeCatalog()

                combine(companyFlow, catalogFlow) { routes, catalog ->
                    val deptNameByCode = catalog?.departments?.associate { it.code to it.name } ?: emptyMap()
                    val provNameByCode = catalog?.provinces?.associate { it.code to it.name } ?: emptyMap()

                    val deptOptions = catalog?.departments
                        ?.map { DeptOption(it.code, it.name) }
                        ?.sortedBy { it.name }
                        ?: emptyList()

                    val provOptionsByDept: Map<String, List<ProvOption>> =
                        catalog?.provinces
                            ?.groupBy({ it.departmentCode }) { ProvOption(it.code, it.departmentCode, it.name) }
                            ?.mapValues { (_, list) -> list.sortedBy { it.name } }
                            ?: emptyMap()

                    val uiItems = routes.map { r ->
                        RouteItemUi(
                            id = r.routeId.value,
                            type = r.routeType,
                            originDeptCode = r.originDeptCode,
                            originDeptName = r.originDeptName ?: deptNameByCode[r.originDeptCode] ?: r.originDeptCode,
                            originProvCode = r.originProvCode,
                            originProvName = r.originProvName ?: r.originProvCode?.let { provNameByCode[it] },
                            destDeptCode = r.destinationDeptCode,
                            destDeptName = r.destinationDeptName ?: deptNameByCode[r.destinationDeptCode] ?: r.destinationDeptCode,
                            destProvCode = r.destinationProvCode,
                            destProvName = r.destinationProvName ?: r.destinationProvCode?.let { provNameByCode[it] },
                            active = r.active
                        )
                    }

                    Triple(uiItems, deptOptions, provOptionsByDept)
                }.collectLatest { (uiItems, deptOptions, provOptionsByDept) ->
                    val filtered = applyFilters(uiItems, _state.value.filters)
                    _state.value = _state.value.copy(
                        isInitializing = false,
                        allRoutes = uiItems,
                        routes = filtered,
                        empty = filtered.isEmpty(),
                        error = null,
                        departments = deptOptions,
                        provincesByDept = provOptionsByDept,
                        geoReady = deptOptions.isNotEmpty()
                    )
                }
            }
        }
    }

    suspend fun getRouteForEdit(routeId: Long): Route? {
        val cid = _state.value.companyId ?: return null
        return runCatching {
            planningRoutesRepository.getRoute(CompanyId(cid), RouteId(routeId))
        }.getOrNull()
    }

    private fun applyFilters(items: List<RouteItemUi>, filters: RouteFilters): List<RouteItemUi> {
        return items.asSequence()
            .filter { route ->
                // Filtro por tipo de ruta
                filters.type == null || route.type == filters.type
            }
            .filter { route ->
                // Filtro por estado activo/inactivo
                filters.activeOnly == null || route.active == filters.activeOnly
            }
            .filter { route ->
                // Filtro por departamento de origen (comparar código)
                filters.originDeptCode == null || route.originDeptCode == filters.originDeptCode
            }
            .filter { route ->
                // Filtro por provincia de origen (comparar código, si se especificó)
                if (filters.originProvCode != null) {
                    route.originProvCode == filters.originProvCode
                } else {
                    true
                }
            }
            .filter { route ->
                // Filtro por departamento de destino (comparar código)
                filters.destDeptCode == null || route.destDeptCode == filters.destDeptCode
            }
            .filter { route ->
                // Filtro por provincia de destino (comparar código, si se especificó)
                if (filters.destProvCode != null) {
                    route.destProvCode == filters.destProvCode
                } else {
                    true
                }
            }
            .filter { route ->
                // Filtro por búsqueda de texto general (buscar en nombres)
                val q = filters.query?.trim()?.lowercase().orEmpty()
                if (q.isEmpty()) {
                    true
                } else {
                    route.originDeptName.lowercase().contains(q) ||
                    (route.originProvName?.lowercase()?.contains(q) == true) ||
                    route.destDeptName.lowercase().contains(q) ||
                    (route.destProvName?.lowercase()?.contains(q) == true)
                }
            }
            .toList()
            .also { }
    }

    fun onRefresh() {
        val cid = _state.value.companyId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            runCatching {
                planningRoutesRepository.refreshRoutesByCompany(CompanyId(cid))
            }.onSuccess {
            }.onFailure { e ->
                _effects.send(Effect.Message(e.message ?: "Error al actualizar"))
            }
            _state.value = _state.value.copy(isRefreshing = false)
        }
    }

    fun onCreate(
        type: RouteType,
        originDeptCode: String,
        originProvCode: String?,
        destDeptCode: String,
        destProvCode: String?,
        active: Boolean
    ) {
        val cid = _state.value.companyId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            runCatching {
                // Validaciones DD vs PP
                val normalizedOriginProv = if (type == RouteType.DD) null else originProvCode
                val normalizedDestProv = if (type == RouteType.DD) null else destProvCode

                val body = RouteCreate(
                    routeType = type,
                    originDeptCode = originDeptCode,
                    originProvCode = (normalizedOriginProv ?: ""),
                    originDistCode = "",
                    destinationDeptCode = destDeptCode,
                    destinationProvCode = (normalizedDestProv ?: ""),
                    destinationDistCode = "",
                    stopDeptCode = null,
                    stopProvCode = null,
                    stopDistCode = null,
                    active = active
                )
                planningRoutesRepository.createRoute(CompanyId(cid), body)
            }.onSuccess {
                _effects.send(Effect.Message("Ruta creada"))
            }.onFailure { e ->
                _effects.send(Effect.Message(e.message ?: "Error al crear ruta"))
            }
            _state.value = _state.value.copy(isSubmitting = false)
        }
    }

    fun onUpdate(
        routeId: Long,
        type: RouteType,
        originDeptCode: String,
        originProvCode: String?,
        destDeptCode: String,
        destProvCode: String?,
        active: Boolean
    ) {
        val cid = _state.value.companyId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            runCatching {
                val normalizedOriginProv = if (type == RouteType.DD) null else originProvCode
                val normalizedDestProv = if (type == RouteType.DD) null else destProvCode

                val body = RouteUpdate(
                    routeType = type,
                    originDeptCode = originDeptCode,
                    originProvCode = (normalizedOriginProv ?: ""),
                    originDistCode = "",
                    destinationDeptCode = destDeptCode,
                    destinationProvCode = (normalizedDestProv ?: ""),
                    destinationDistCode = "",
                    stopDeptCode = null,
                    stopProvCode = null,
                    stopDistCode = null,
                    active = active
                )
                planningRoutesRepository.updateRoute(CompanyId(cid), RouteId(routeId), body)
            }.onSuccess {
                _effects.send(Effect.Message("Ruta actualizada"))
            }.onFailure { e ->
                _effects.send(Effect.Message(e.message ?: "Error al actualizar ruta"))
            }
            _state.value = _state.value.copy(isSubmitting = false)
        }
    }

    fun onDelete(routeId: Long) {
        val cid = _state.value.companyId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            runCatching {
                planningRoutesRepository.deleteRoute(CompanyId(cid), RouteId(routeId))
            }.onSuccess {
                _effects.send(Effect.Message("Ruta eliminada"))
            }.onFailure { e ->
                _effects.send(Effect.Message(e.message ?: "Error al eliminar ruta"))
            }
            _state.value = _state.value.copy(isSubmitting = false)
        }
    }

    fun onFiltersChanged(newFilters: RouteFilters) {
        val filtered = applyFilters(_state.value.allRoutes, newFilters)
        _state.value = _state.value.copy(filters = newFilters, routes = filtered, empty = filtered.isEmpty())
    }
}


