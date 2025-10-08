package com.wapps1.redcarga.features.fleet.domain.models.vehicles

data class VehicleUpsert(
    val name: String,
    val plate: String,
    val active: Boolean
)

data class CreateVehicleResult(val vehicleId: Long)


