package com.wapps1.redcarga.features.fleet.data.repositories

 
import com.wapps1.redcarga.features.fleet.data.network.toFleetDomainError
import com.wapps1.redcarga.features.fleet.data.local.dao.VehiclesDao
import com.wapps1.redcarga.features.fleet.data.local.entities.VehicleEntity
import com.wapps1.redcarga.features.fleet.data.mappers.*
import com.wapps1.redcarga.features.fleet.data.remote.services.FleetVehiclesService
import com.wapps1.redcarga.features.fleet.domain.DomainError
import com.wapps1.redcarga.features.fleet.domain.DomainException
import com.wapps1.redcarga.features.fleet.domain.models.common.CompanyId
import com.wapps1.redcarga.features.fleet.domain.models.common.VehicleId
import com.wapps1.redcarga.features.fleet.domain.models.vehicles.CreateVehicleResult
import com.wapps1.redcarga.features.fleet.domain.models.vehicles.Vehicle
import com.wapps1.redcarga.features.fleet.domain.models.vehicles.VehicleUpsert
import com.wapps1.redcarga.features.fleet.domain.repositories.FleetVehiclesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

 

class FleetVehiclesRepositoryImpl @Inject constructor(
    private val service: FleetVehiclesService,
    private val dao: VehiclesDao
) : FleetVehiclesRepository {

    override fun observeVehicles(companyId: CompanyId): Flow<List<Vehicle>> =
        dao.observeVehicles(companyId.value).map { list -> list.map { e -> e.toDomain() } }

    override fun observeVehicle(vehicleId: VehicleId): Flow<Vehicle?> =
        dao.observeVehicle(vehicleId.value).map { it?.toDomain() }

    override suspend fun refreshVehicles(companyId: CompanyId) = withContext(Dispatchers.IO) {
        runCatching {
            val remote = service.listVehicles(companyId.value).map { it.toEntity() }
            dao.replaceAllForCompany(companyId.value, remote)
            Unit
        }.getOrElse { e ->
            throw DomainException(e.toFleetDomainError(), e)
        }
    }

    override suspend fun getVehicle(vehicleId: VehicleId): Vehicle = withContext(Dispatchers.IO) {
        runCatching { service.getVehicle(vehicleId.value).toDomain() }
            .getOrElse { e -> throw DomainException(e.toFleetDomainError(), e) }
    }

    override suspend fun createVehicle(companyId: CompanyId, body: VehicleUpsert): CreateVehicleResult =
        withContext(Dispatchers.IO) {
            runCatching {
                val res = service.createVehicle(companyId.value, body.toDto())
                val now = System.currentTimeMillis()
                val cached = VehicleEntity(
                    vehicleId = res.vehicleId,
                    companyId = companyId.value,
                    name = body.name,
                    plate = body.plate,
                    active = body.active,
                    createdAt = now,
                    updatedAt = now,
                    dirty = false,
                    deletedLocally = false
                )
                dao.upsert(cached)
                CreateVehicleResult(res.vehicleId)
            }.getOrElse { e ->
                throw DomainException(e.toFleetDomainError(), e)
            }
        }

    override suspend fun updateVehicle(vehicleId: VehicleId, body: VehicleUpsert) = withContext(Dispatchers.IO) {
        runCatching {
            service.updateVehicle(vehicleId.value, body.toDto())
            // Reflejar cambios en Room inmediatamente para que la UI se actualice sin refrescar
            val existing = dao.find(vehicleId.value)
            if (existing != null) {
                val now = System.currentTimeMillis()
                dao.upsert(
                    existing.copy(
                        name = body.name,
                        plate = body.plate,
                        active = body.active,
                        updatedAt = now,
                        dirty = false,
                        deletedLocally = false
                    )
                )
            } else {
                // Si no existe en cache, al menos marcar sincronizado
                dao.markAsSynced(vehicleId.value)
            }
            Unit
        }.getOrElse { e ->
            throw DomainException(e.toFleetDomainError(), e)
        }
    }

    override suspend fun deleteVehicle(vehicleId: VehicleId) = withContext(Dispatchers.IO) {
        runCatching {
            service.deleteVehicle(vehicleId.value)
            // Eliminar de Room inmediatamente para reflejar en UI
            dao.deleteById(vehicleId.value)
            Unit
        }.getOrElse { e ->
            throw DomainException(e.toFleetDomainError(), e)
        }
    }
}


