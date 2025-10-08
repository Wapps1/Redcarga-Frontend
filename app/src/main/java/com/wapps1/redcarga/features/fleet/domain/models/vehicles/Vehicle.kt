package com.wapps1.redcarga.features.fleet.domain.models.vehicles

import com.wapps1.redcarga.features.fleet.domain.models.common.CompanyId
import com.wapps1.redcarga.features.fleet.domain.models.common.VehicleId

data class Vehicle(
    val vehicleId: VehicleId,
    val companyId: CompanyId,
    val name: String,
    val plate: String,
    val active: Boolean,
    val createdAt: Long?,
    val updatedAt: Long?
)


