package com.wapps1.redcarga.features.fleet.data.repositories

import com.wapps1.redcarga.features.fleet.data.network.toFleetDomainError
import com.wapps1.redcarga.features.fleet.data.local.dao.DriversDao
import com.wapps1.redcarga.features.fleet.data.local.entities.DriverEntity
import com.wapps1.redcarga.features.fleet.data.mappers.*
import com.wapps1.redcarga.features.fleet.data.remote.services.FleetDriversService
import com.wapps1.redcarga.features.fleet.domain.DomainException
import com.wapps1.redcarga.features.fleet.domain.models.common.CompanyId
import com.wapps1.redcarga.features.fleet.domain.models.common.DriverId
import com.wapps1.redcarga.features.fleet.domain.models.drivers.CreateDriverResult
import com.wapps1.redcarga.features.fleet.domain.models.drivers.Driver
import com.wapps1.redcarga.features.fleet.domain.models.drivers.DriverUpsert
import com.wapps1.redcarga.features.fleet.domain.repositories.FleetDriversRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

 

class FleetDriversRepositoryImpl @Inject constructor(
    private val service: FleetDriversService,
    private val dao: DriversDao
) : FleetDriversRepository {

    override fun observeDrivers(companyId: CompanyId): Flow<List<Driver>> =
        dao.observeDrivers(companyId.value).map { list -> list.map { it.toDomain() } }

    override fun observeDriver(driverId: DriverId): Flow<Driver?> =
        dao.observeDriver(driverId.value).map { it?.toDomain() }

    override suspend fun refreshDrivers(companyId: CompanyId) = withContext(Dispatchers.IO) {
        runCatching {
            val remote = service.listDrivers(companyId.value).map { it.toEntity() }
            dao.replaceAllForCompany(companyId.value, remote)
            Unit
        }.getOrElse { e ->
            throw DomainException(e.toFleetDomainError(), e)
        }
    }

    override suspend fun getDriver(driverId: DriverId): Driver = withContext(Dispatchers.IO) {
        runCatching { service.getDriver(driverId.value).toDomain() }
            .getOrElse { e -> throw DomainException(e.toFleetDomainError(), e) }
    }

    override suspend fun createDriver(companyId: CompanyId, body: DriverUpsert): CreateDriverResult =
        withContext(Dispatchers.IO) {
            runCatching {
                val res = service.createDriver(companyId.value, body.toDto())
                val now = System.currentTimeMillis()
                val cached = DriverEntity(
                    driverId = res.driverId,
                    companyId = companyId.value,
                    firstName = body.firstName,
                    lastName = body.lastName,
                    email = body.email.value,
                    phone = body.phone,
                    licenseNumber = body.licenseNumber,
                    active = body.active,
                    createdAt = now,
                    updatedAt = now,
                    dirty = false,
                    deletedLocally = false
                )
                dao.upsert(cached)
                CreateDriverResult(res.driverId)
            }.getOrElse { e ->
                throw DomainException(e.toFleetDomainError(), e)
            }
        }

    override suspend fun updateDriver(driverId: DriverId, body: DriverUpsert) =
        withContext(Dispatchers.IO) {
        runCatching {
                service.updateDriver(driverId.value, body.toDto())
                // Refrescar instantáneamente en Room
                val existing = dao.find(driverId.value)
                if (existing != null) {
                    val now = System.currentTimeMillis()
                    dao.upsert(
                        existing.copy(
                            firstName = body.firstName,
                            lastName = body.lastName,
                            email = body.email.value,
                            phone = body.phone,
                            licenseNumber = body.licenseNumber,
                            active = body.active,
                            updatedAt = now,
                            dirty = false,
                            deletedLocally = false
                        )
                    )
                } else {
                    dao.markAsSynced(driverId.value)
                }
            Unit
        }.getOrElse { e ->
            throw DomainException(e.toFleetDomainError(), e)
        }
        }

    override suspend fun deleteDriver(driverId: DriverId) = withContext(Dispatchers.IO) {
        runCatching {
            service.deleteDriver(driverId.value)
            dao.deleteById(driverId.value)
            Unit
        }.getOrElse { e ->
            throw DomainException(e.toFleetDomainError(), e)
        }
    }
}


