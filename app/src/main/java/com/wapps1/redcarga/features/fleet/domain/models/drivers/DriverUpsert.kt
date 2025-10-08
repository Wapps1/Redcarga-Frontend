package com.wapps1.redcarga.features.fleet.domain.models.drivers

import com.wapps1.redcarga.features.auth.domain.models.value.Email

data class DriverUpsert(
    val firstName: String,
    val lastName: String,
    val email: Email,
    val phone: String,
    val licenseNumber: String,
    val active: Boolean
)

data class CreateDriverResult(val driverId: Long)


