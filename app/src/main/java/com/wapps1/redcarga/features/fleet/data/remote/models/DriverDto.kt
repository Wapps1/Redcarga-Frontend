package com.wapps1.redcarga.features.fleet.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DriverDto(
    @Json(name = "driverId") val driverId: Long,
    @Json(name = "companyId") val companyId: Long,
    @Json(name = "fullName") val fullName: String,  // Backend retorna fullName, no firstName/lastName
    @Json(name = "docNumber") val docNumber: String,  // Backend retorna docNumber
    @Json(name = "phone") val phone: String,
    @Json(name = "licenseNumber") val licenseNumber: String,
    @Json(name = "active") val active: Boolean,
    @Json(name = "createdAt") val createdAt: String?,  // ISO 8601 string, no Long
    @Json(name = "updatedAt") val updatedAt: String?   // ISO 8601 string, no Long
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

// DTO específico para crear conductor desde el flujo de registro (usa accountId)
data class CreateDriverFromAccountDto(
    val accountId: Long,
    val licenseNumber: String?,
    val active: Boolean
)


