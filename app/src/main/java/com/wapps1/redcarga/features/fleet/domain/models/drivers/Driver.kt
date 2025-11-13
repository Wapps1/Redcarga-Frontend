package com.wapps1.redcarga.features.fleet.domain.models.drivers

import com.wapps1.redcarga.features.fleet.domain.models.common.CompanyId
import com.wapps1.redcarga.features.fleet.domain.models.common.DriverId
import com.wapps1.redcarga.features.auth.domain.models.value.Email

data class Driver(
    val driverId: DriverId,
    val companyId: CompanyId,
    val firstName: String,
    val lastName: String,
    val email: Email,
    val phone: String,
    val licenseNumber: String,
    val active: Boolean,
    val createdAt: Long?,
    val updatedAt: Long?
)


