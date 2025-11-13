package com.wapps1.redcarga.features.fleet.data.repositories

import android.util.Log
import com.wapps1.redcarga.features.fleet.data.network.toFleetDomainError
import com.wapps1.redcarga.features.fleet.data.local.dao.RoutesDao
import com.wapps1.redcarga.features.fleet.data.mappers.*
import com.wapps1.redcarga.features.fleet.data.remote.services.PlanningRoutesService
import com.wapps1.redcarga.features.fleet.domain.DomainException
import com.wapps1.redcarga.features.fleet.domain.models.common.CompanyId
import com.wapps1.redcarga.features.fleet.domain.models.common.ProviderId
import com.wapps1.redcarga.features.fleet.domain.models.common.RouteId
import com.wapps1.redcarga.features.fleet.domain.models.routes.CreateRouteResult
import com.wapps1.redcarga.features.fleet.domain.models.routes.Route
import com.wapps1.redcarga.features.fleet.domain.models.routes.RouteCreate
import com.wapps1.redcarga.features.fleet.domain.models.routes.RouteType
import com.wapps1.redcarga.features.fleet.domain.models.routes.RouteUpdate
import com.wapps1.redcarga.features.fleet.domain.repositories.PlanningRoutesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG_ROUTES_REPO = "PlanningRoutesRepo"

class PlanningRoutesRepositoryImpl @Inject constructor(
    private val service: PlanningRoutesService,
    private val dao: RoutesDao
) : PlanningRoutesRepository {

    override fun observeRoutesByProvider(providerId: ProviderId): Flow<List<Route>> {
        // Sin cache por providerId (el cache es por companyId). Podrías resolver companyId desde perfil.
        return dao.observeRoutes(companyId = -1).map { it.map { e -> e.toDomain() } }
    }

    override fun observeRoutesByCompany(companyId: CompanyId): Flow<List<Route>> =
        dao.observeRoutes(companyId.value).map { list ->
            val ordered = list.asReversed()
            Log.d(TAG_ROUTES_REPO, "observeRoutesByCompany(${companyId.value}) -> ${ordered.size} items from Room (reversed)")
            ordered.map { e -> e.toDomain() }
        }

    override fun observeRoute(companyId: CompanyId, routeId: RouteId): Flow<Route?> =
        dao.observeRoute(companyId.value, routeId.value).map { it?.toDomain() }

    override suspend fun refreshRoutesByProvider(providerId: ProviderId) = withContext(Dispatchers.IO) {
        Log.d(TAG_ROUTES_REPO, "refreshRoutesByProvider(${providerId.value})")
        runCatching {
            val remote = service.listRoutesByProvider(providerId.value).map { it.toEntity() }
            // Si las rutas son de múltiples compañías, podrías agrupar por companyId y hacer replace parcial.
            remote.groupBy { it.companyId }.forEach { (companyId, list) ->
                dao.replaceAllForCompany(companyId, list)
            }
            Log.d(TAG_ROUTES_REPO, "refreshRoutesByProvider OK: total=${remote.size}")
            Unit
        }.getOrElse { e ->
            Log.e(TAG_ROUTES_REPO, "refreshRoutesByProvider failed", e)
            throw DomainException(e.toFleetDomainError(), e)
        }
    }

    override suspend fun refreshRoutesByCompany(companyId: CompanyId) = withContext(Dispatchers.IO) {
        Log.d(TAG_ROUTES_REPO, "refreshRoutesByCompany(${companyId.value})")
        runCatching {
            val remote = service.listRoutesByCompany(companyId.value).map { it.toEntity() }
            dao.replaceAllForCompany(companyId.value, remote)
            Log.d(TAG_ROUTES_REPO, "refreshRoutesByCompany OK: total=${remote.size}")
            Unit
        }.getOrElse { e ->
            Log.e(TAG_ROUTES_REPO, "refreshRoutesByCompany failed", e)
            throw DomainException(e.toFleetDomainError(), e)
        }
    }

    override suspend fun refreshRoute(companyId: CompanyId, routeId: RouteId) = withContext(Dispatchers.IO) {
        Log.d(TAG_ROUTES_REPO, "refreshRoute(c=${companyId.value}, r=${routeId.value})")
        runCatching {
            val dto = service.getRoute(companyId.value, routeId.value)
            dao.upsert(dto.toEntity())
            Log.d(TAG_ROUTES_REPO, "refreshRoute OK")
            Unit
        }.getOrElse { e ->
            Log.e(TAG_ROUTES_REPO, "refreshRoute failed", e)
            throw DomainException(e.toFleetDomainError(), e)
        }
    }

    override suspend fun getRoute(companyId: CompanyId, routeId: RouteId): Route = withContext(Dispatchers.IO) {
        Log.d(TAG_ROUTES_REPO, "getRoute(c=${companyId.value}, r=${routeId.value})")
        runCatching { service.getRoute(companyId.value, routeId.value).toDomain() }
            .onSuccess { Log.d(TAG_ROUTES_REPO, "getRoute OK") }
            .getOrElse { e ->
                Log.e(TAG_ROUTES_REPO, "getRoute failed", e)
                throw DomainException(e.toFleetDomainError(), e)
            }
    }

    override suspend fun createRoute(companyId: CompanyId, body: RouteCreate): CreateRouteResult = withContext(Dispatchers.IO) {
        Log.d(TAG_ROUTES_REPO, "createRoute(c=${companyId.value}) body=$body")
        runCatching {
            val res = service.createRoute(companyId.value, body.toDto())
            // Insert optimista, sin nombres geo (se resolverán con GeoRepository)
            dao.upsert(
                com.wapps1.redcarga.features.fleet.data.local.entities.RouteEntity(
                    routeId = res.routeId,
                    companyId = companyId.value,
                    companyName = null,
                    routeType = when (body.routeType) { RouteType.DD -> "DD"; RouteType.PP -> "PP" },
                    originDepartmentCode = body.originDeptCode,
                    originProvinceCode = body.originProvCode,
                    destDepartmentCode = body.destinationDeptCode,
                    destProvinceCode = body.destinationProvCode,
                    originDepartmentName = null,
                    originProvinceName = null,
                    destDepartmentName = null,
                    destProvinceName = null,
                    active = body.active
                )
            )
            Log.d(TAG_ROUTES_REPO, "createRoute OK id=${res.routeId}")
            CreateRouteResult(res.routeId)
        }.getOrElse { e ->
            Log.e(TAG_ROUTES_REPO, "createRoute failed", e)
            throw DomainException(e.toFleetDomainError(), e)
        }
    }

    override suspend fun updateRoute(companyId: CompanyId, routeId: RouteId, body: RouteUpdate) = withContext(Dispatchers.IO) {
        Log.d(TAG_ROUTES_REPO, "updateRoute(c=${companyId.value}, r=${routeId.value}) body=$body")
        runCatching {
            service.updateRoute(companyId.value, routeId.value, body.toDto())
            // Opcional: actualizar campos clave para reflejar al instante
            dao.markAsSynced(routeId.value)
            // Volver a cargar una sola ruta para evitar esperar a un refresh completo
            val dto = service.getRoute(companyId.value, routeId.value)
            dao.upsert(dto.toEntity())
            Log.d(TAG_ROUTES_REPO, "updateRoute OK")
            Unit
        }.getOrElse { e ->
            Log.e(TAG_ROUTES_REPO, "updateRoute failed", e)
            throw DomainException(e.toFleetDomainError(), e)
        }
    }

    override suspend fun deleteRoute(companyId: CompanyId, routeId: RouteId) = withContext(Dispatchers.IO) {
        Log.d(TAG_ROUTES_REPO, "deleteRoute(c=${companyId.value}, r=${routeId.value})")
        runCatching {
            service.deleteRoute(companyId.value, routeId.value)
            // Remover inmediatamente de Room para que la UI refleje el cambio
            dao.deleteById(routeId.value)
            Unit
        }.getOrElse { e ->
            Log.e(TAG_ROUTES_REPO, "deleteRoute failed", e)
            throw DomainException(e.toFleetDomainError(), e)
        }
    }
}


