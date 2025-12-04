package com.wapps1.redcarga.features.fleet.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ⚠️ NOTA: Los DTOs para Paso 1 y Paso 2 ya no se usan.
// Ahora se usan directamente los DTOs del módulo auth:
// - RegisterStartRequestDto / RegisterStartResponseDto (Paso 1)
// - PersonCreateRequestDto / PersonCreateResponseDto (Paso 2)
// Estos DTOs se mantienen aquí por compatibilidad, pero no se utilizan en el código actual.

// Paso 1: Register Start (DEPRECADO - usar RegisterStartRequestDto del módulo auth)
@Deprecated("Usar RegisterStartRequestDto del módulo auth", ReplaceWith("RegisterStartRequestDto"))
@JsonClass(generateAdapter = true)
data class DriverRegistrationStartRequestDto(
    @Json(name = "email") val email: String,
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String,
    @Json(name = "roleCode") val roleCode: String = "PROVIDER",
    @Json(name = "platform") val platform: String,
    @Json(name = "idempotencyKey") val idempotencyKey: String?
)

@Deprecated("Usar RegisterStartResponseDto del módulo auth", ReplaceWith("RegisterStartResponseDto"))
@JsonClass(generateAdapter = true)
data class DriverRegistrationStartResponseDto(
    @Json(name = "accountId") val accountId: Long,
    @Json(name = "signupIntentId") val signupIntentId: Long,
    @Json(name = "email") val email: String,
    @Json(name = "emailVerified") val emailVerified: Boolean,
    @Json(name = "verificationLink") val verificationLink: String?
)

// Paso 2: Identity Verification (DEPRECADO - usar PersonCreateRequestDto del módulo auth)
@Deprecated("Usar PersonCreateRequestDto del módulo auth", ReplaceWith("PersonCreateRequestDto"))
@JsonClass(generateAdapter = true)
data class DriverIdentityVerificationRequestDto(
    @Json(name = "accountId") val accountId: Long,
    @Json(name = "fullName") val fullName: String,
    @Json(name = "docTypeCode") val docTypeCode: String,
    @Json(name = "docNumber") val docNumber: String,
    @Json(name = "birthDate") val birthDate: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "ruc") val ruc: String
)

@Deprecated("Usar PersonCreateResponseDto del módulo auth", ReplaceWith("PersonCreateResponseDto"))
@JsonClass(generateAdapter = true)
data class DriverIdentityVerificationResponseDto(
    @Json(name = "passed") val passed: Boolean,
    @Json(name = "personId") val personId: Long
)

// Paso 3: Company Association
@JsonClass(generateAdapter = true)
data class DriverCompanyAssociationRequestDto(
    @Json(name = "operatorId") val operatorId: Long,
    @Json(name = "roleId") val roleId: Int
)

