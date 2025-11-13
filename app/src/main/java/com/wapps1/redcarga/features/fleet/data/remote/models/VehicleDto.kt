package com.wapps1.redcarga.features.fleet.data.remote.models

data class VehicleDto(
    val vehicleId: Long,
    val companyId: Long,
    val name: String,
    val plate: String,
    val active: Boolean,
    val createdAt: Long?,
    val updatedAt: Long?
)

data class VehicleUpsertDto(
    val name: String,
    val plate: String,
    val active: Boolean
)

data class CreateVehicleResponseDto(val ok: Boolean, val vehicleId: Long)


