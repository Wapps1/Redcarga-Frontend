package com.wapps1.redcarga.features.fleet.data.remote.models

data class DriverDto(
    val driverId: Long,
    val companyId: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val licenseNumber: String,
    val active: Boolean,
    val createdAt: Long?,
    val updatedAt: Long?
)

data class DriverUpsertDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val licenseNumber: String,
    val active: Boolean
)

data class CreateDriverResponseDto(val ok: Boolean, val driverId: Long)


