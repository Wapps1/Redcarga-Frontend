package com.wapps1.redcarga.features.fleet.domain.repositories

import com.wapps1.redcarga.features.fleet.domain.models.common.CompanyId
import com.wapps1.redcarga.features.fleet.domain.models.common.ProviderId
import com.wapps1.redcarga.features.fleet.domain.models.common.RouteId
import com.wapps1.redcarga.features.fleet.domain.models.routes.CreateRouteResult
import com.wapps1.redcarga.features.fleet.domain.models.routes.Route
import com.wapps1.redcarga.features.fleet.domain.models.routes.RouteCreate
import com.wapps1.redcarga.features.fleet.domain.models.routes.RouteUpdate
import kotlinx.coroutines.flow.Flow

interface PlanningRoutesRepository {
    fun observeRoutesByProvider(providerId: ProviderId): Flow<List<Route>>
    fun observeRoutesByCompany(companyId: CompanyId): Flow<List<Route>>
    fun observeRoute(companyId: CompanyId, routeId: RouteId): Flow<Route?>

    suspend fun refreshRoutesByProvider(providerId: ProviderId)
    suspend fun refreshRoutesByCompany(companyId: CompanyId)
    suspend fun refreshRoute(companyId: CompanyId, routeId: RouteId)

    suspend fun getRoute(companyId: CompanyId, routeId: RouteId): Route
    suspend fun createRoute(companyId: CompanyId, body: RouteCreate): CreateRouteResult
    suspend fun updateRoute(companyId: CompanyId, routeId: RouteId, body: RouteUpdate)
    suspend fun deleteRoute(companyId: CompanyId, routeId: RouteId)

}


