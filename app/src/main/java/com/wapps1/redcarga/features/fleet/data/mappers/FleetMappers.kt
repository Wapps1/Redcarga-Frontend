package com.wapps1.redcarga.features.fleet.data.mappers

import com.wapps1.redcarga.features.auth.data.remote.models.RegisterStartRequestDto
import com.wapps1.redcarga.features.auth.data.remote.models.RegisterStartResponseDto
import com.wapps1.redcarga.features.auth.data.remote.models.PersonCreateRequestDto
import com.wapps1.redcarga.features.auth.data.remote.models.PersonCreateResponseDto
import com.wapps1.redcarga.features.auth.domain.models.value.Email
import com.wapps1.redcarga.features.fleet.data.local.entities.*
import com.wapps1.redcarga.features.fleet.data.remote.models.*
import com.wapps1.redcarga.features.fleet.domain.models.common.*
import com.wapps1.redcarga.features.fleet.domain.models.drivers.*
import com.wapps1.redcarga.features.fleet.domain.models.vehicles.*
import com.wapps1.redcarga.features.fleet.domain.models.routes.*
import com.wapps1.redcarga.features.fleet.domain.models.geo.*
import java.time.Instant

// Helper para convertir ISO 8601 string a Long (timestamp en millis)
// Sigue el mismo patrón que RequestsMappers y DealsMappers
private fun String?.toTimestampMillis(): Long? {
    return if (this == null) null else {
        try {
            Instant.parse(this).toEpochMilli()
        } catch (e: Exception) {
            null  // Retornar null si falla el parsing
        }
    }
}

// Helper para dividir fullName en firstName y lastName
private fun String.splitFullName(): Pair<String, String> {
    val parts = this.trim().split("\\s+".toRegex(), limit = 2)
    return when (parts.size) {
        0 -> Pair("", "")
        1 -> Pair(parts[0], "")
        else -> Pair(parts[0], parts[1])
    }
}

fun DriverDto.toDomain() = run {
    val (firstName, lastName) = fullName.splitFullName()
    Driver(
        driverId = DriverId(driverId),
        companyId = CompanyId(companyId),
        firstName = firstName,
        lastName = lastName,
        email = Email("no-email@placeholder.com"), // ✅ Email placeholder válido (backend no retorna email)
        phone = phone,
        licenseNumber = licenseNumber,
        active = active,
        createdAt = createdAt.toTimestampMillis(),
        updatedAt = updatedAt.toTimestampMillis()
    )
}

fun VehicleDto.toDomain() = Vehicle(
    vehicleId = VehicleId(vehicleId),
    companyId = CompanyId(companyId),
    name = name,
    plate = plate,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun RouteDto.toDomain() = Route(
    routeId = RouteId(routeId),
    companyId = CompanyId(companyId),
    companyName = companyName,
    routeType = RouteType.valueOf(routeType),
    originDeptCode = originDepartmentCode,
    originProvCode = originProvinceCode,
    originDistCode = null,
    originDeptName = originDepartmentName,
    originProvName = originProvinceName,
    originDistName = null,
    destinationDeptCode = destDepartmentCode,
    destinationProvCode = destProvinceCode,
    destinationDistCode = null,
    destinationDeptName = destDepartmentName,
    destinationProvName = destProvinceName,
    destinationDistName = null,
    stopDeptCode = null,
    stopProvCode = null,
    stopDistCode = null,
    stopDeptName = null,
    stopProvName = null,
    stopDistName = null,
    active = active
)

fun GeoCatalogDto.toDomain() = GeoCatalog(
    departments = departments.map { Department(it.code, it.name) },
    provinces = provinces.map { Province(it.code, it.departmentCode, it.name) }
)

fun DriverUpsert.toDto() = DriverUpsertDto(
    firstName = firstName,
    lastName = lastName,
    email = email.value,
    phone = phone,
    licenseNumber = licenseNumber,
    active = active
)

fun VehicleUpsert.toDto() = VehicleUpsertDto(
    name = name,
    plate = plate,
    active = active
)

fun RouteCreate.toDto() = RouteCreateDto(
    routeTypeId = when (routeType) { RouteType.DD -> 1; RouteType.PP -> 2 },
    originDepartmentCode = originDeptCode,
    destDepartmentCode = destinationDeptCode,
    originProvinceCode = originProvCode,
    destProvinceCode = destinationProvCode,
    active = active
)

fun RouteUpdate.toDto() = RouteUpdateDto(
    routeTypeId = when (routeType) { RouteType.DD -> 1; RouteType.PP -> 2 },
    originDepartmentCode = originDeptCode,
    originProvinceCode = originProvCode,
    destDepartmentCode = destinationDeptCode,
    destProvinceCode = destinationProvCode,
    active = active
)

fun DriverDto.toEntity() = run {
    val (firstName, lastName) = fullName.splitFullName()
    DriverEntity(
        driverId = driverId,
        companyId = companyId,
        firstName = firstName,
        lastName = lastName,
        email = "no-email@placeholder.com", // ✅ Consistente con el dominio (backend no retorna email)
        phone = phone,
        licenseNumber = licenseNumber,
        active = active,
        createdAt = createdAt.toTimestampMillis(),
        updatedAt = updatedAt.toTimestampMillis()
    )
}

fun VehicleDto.toEntity() = VehicleEntity(
    vehicleId = vehicleId,
    companyId = companyId,
    name = name,
    plate = plate,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun RouteDto.toEntity() = RouteEntity(
    routeId = routeId,
    companyId = companyId,
    companyName = companyName,
    routeType = routeType,
    originDepartmentCode = originDepartmentCode,
    originProvinceCode = originProvinceCode,
    destDepartmentCode = destDepartmentCode,
    destProvinceCode = destProvinceCode,
    originDepartmentName = originDepartmentName,
    originProvinceName = originProvinceName,
    destDepartmentName = destDepartmentName,
    destProvinceName = destProvinceName,
    active = active
)

fun DriverEntity.toDomain() = Driver(
    driverId = DriverId(driverId),
    companyId = CompanyId(companyId),
    firstName = firstName,
    lastName = lastName,
    email = Email(if (email.isBlank()) "no-email@placeholder.com" else email), // ✅ Manejar emails vacíos de datos antiguos en Room
    phone = phone,
    licenseNumber = licenseNumber,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun VehicleEntity.toDomain() = Vehicle(
    vehicleId = VehicleId(vehicleId),
    companyId = CompanyId(companyId),
    name = name,
    plate = plate,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun RouteEntity.toDomain() = Route(
    routeId = RouteId(routeId),
    companyId = CompanyId(companyId),
    companyName = companyName,
    routeType = RouteType.valueOf(routeType),
    originDeptCode = originDepartmentCode,
    originProvCode = originProvinceCode,
    originDistCode = null,
    originDeptName = originDepartmentName,
    originProvName = originProvinceName,
    originDistName = null,
    destinationDeptCode = destDepartmentCode,
    destinationProvCode = destProvinceCode,
    destinationDistCode = null,
    destinationDeptName = destDepartmentName,
    destinationProvName = destProvinceName,
    destinationDistName = null,
    stopDeptCode = null,
    stopProvCode = null,
    stopDistCode = null,
    stopDeptName = null,
    stopProvName = null,
    stopDistName = null,
    active = active
)

// ========== MAPPERS PARA REGISTRO DE CONDUCTOR ==========

// Paso 1: Register Start
// ✅ CORRECCIÓN: Usar RegisterStartRequestDto del módulo auth (no crear uno nuevo)
fun DriverRegistrationStartRequest.toRegisterStartRequestDto(): RegisterStartRequestDto = 
    RegisterStartRequestDto(
        email = email.value,
        username = username,
        password = password,
        roleCode = "PROVIDER",
        platform = platform
        // ⚠️ idempotencyKey se omite porque RegisterStartRequestDto no lo tiene
        // Es opcional en el backend, así que no es crítico
    )

// ✅ CORRECCIÓN: Mapear RegisterStartResponseDto del módulo auth a DriverRegistrationStartResult
fun RegisterStartResponseDto.toDriverRegistrationStartResult(): DriverRegistrationStartResult = 
    DriverRegistrationStartResult(
        accountId = accountId,
        signupIntentId = signupIntentId,
        email = email,
        emailVerified = emailVerified,
        verificationLink = verificationLink // Ya no es nullable
    )

// Paso 2: Identity Verification
// ✅ CORRECCIÓN: Usar PersonCreateRequestDto del módulo auth (ya existe y es compatible)
fun DriverIdentityVerificationRequest.toPersonCreateRequestDto(): PersonCreateRequestDto = 
    PersonCreateRequestDto(
        accountId = accountId,
        fullName = fullName,
        docTypeCode = docTypeCode,
        docNumber = docNumber,
        birthDate = birthDate,
        phone = phone,
        ruc = ruc
    )

// ✅ CORRECCIÓN: Mapear PersonCreateResponseDto del módulo auth a DriverIdentityVerificationResult
fun PersonCreateResponseDto.toDriverIdentityVerificationResult(): DriverIdentityVerificationResult = 
    DriverIdentityVerificationResult(
        passed = passed,
        personId = personId
    )

// Paso 3: Company Association
fun DriverCompanyAssociationRequest.toDto() = DriverCompanyAssociationRequestDto(
    operatorId = operatorId,
    roleId = roleId
)


