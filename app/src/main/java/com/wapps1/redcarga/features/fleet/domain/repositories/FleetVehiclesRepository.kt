package com.wapps1.redcarga.features.fleet.domain.repositories

import com.wapps1.redcarga.features.fleet.domain.models.common.CompanyId
import com.wapps1.redcarga.features.fleet.domain.models.common.VehicleId
import com.wapps1.redcarga.features.fleet.domain.models.vehicles.CreateVehicleResult
import com.wapps1.redcarga.features.fleet.domain.models.vehicles.Vehicle
import com.wapps1.redcarga.features.fleet.domain.models.vehicles.VehicleUpsert
import kotlinx.coroutines.flow.Flow

interface FleetVehiclesRepository {
    fun observeVehicles(companyId: CompanyId): Flow<List<Vehicle>>
    fun observeVehicle(vehicleId: VehicleId): Flow<Vehicle?>

    suspend fun refreshVehicles(companyId: CompanyId)

    suspend fun getVehicle(vehicleId: VehicleId): Vehicle
    suspend fun createVehicle(companyId: CompanyId, body: VehicleUpsert): CreateVehicleResult
    suspend fun updateVehicle(vehicleId: VehicleId, body: VehicleUpsert)
    suspend fun deleteVehicle(vehicleId: VehicleId)
}


