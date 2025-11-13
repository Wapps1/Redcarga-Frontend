package com.wapps1.redcarga.features.fleet.domain.repositories

import com.wapps1.redcarga.features.fleet.domain.models.common.CompanyId
import com.wapps1.redcarga.features.fleet.domain.models.common.DriverId
import com.wapps1.redcarga.features.fleet.domain.models.drivers.CreateDriverResult
import com.wapps1.redcarga.features.fleet.domain.models.drivers.Driver
import com.wapps1.redcarga.features.fleet.domain.models.drivers.DriverUpsert
import kotlinx.coroutines.flow.Flow

interface FleetDriversRepository {
    fun observeDrivers(companyId: CompanyId): Flow<List<Driver>>

    fun observeDriver(driverId: DriverId): Flow<Driver?>

    suspend fun refreshDrivers(companyId: CompanyId)

    suspend fun getDriver(driverId: DriverId): Driver
    suspend fun createDriver(companyId: CompanyId, body: DriverUpsert): CreateDriverResult
    suspend fun updateDriver(driverId: DriverId, body: DriverUpsert)
    suspend fun deleteDriver(driverId: DriverId)
}


