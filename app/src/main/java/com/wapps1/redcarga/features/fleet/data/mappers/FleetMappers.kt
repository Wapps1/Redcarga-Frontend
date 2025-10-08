package com.wapps1.redcarga.features.fleet.data.mappers

import com.wapps1.redcarga.features.auth.domain.models.value.Email
import com.wapps1.redcarga.features.fleet.data.local.entities.*
import com.wapps1.redcarga.features.fleet.data.remote.models.*
import com.wapps1.redcarga.features.fleet.domain.models.common.*
import com.wapps1.redcarga.features.fleet.domain.models.drivers.*
import com.wapps1.redcarga.features.fleet.domain.models.vehicles.*
import com.wapps1.redcarga.features.fleet.domain.models.routes.*
import com.wapps1.redcarga.features.fleet.domain.models.geo.*

fun DriverDto.toDomain() = Driver(
    driverId = DriverId(driverId),
    companyId = CompanyId(companyId),
    firstName = firstName,
    lastName = lastName,
    email = Email(email),
    phone = phone,
    licenseNumber = licenseNumber,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt
)

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

fun DriverDto.toEntity() = DriverEntity(
    driverId = driverId,
    companyId = companyId,
    firstName = firstName,
    lastName = lastName,
    email = email,
    phone = phone,
    licenseNumber = licenseNumber,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt
)

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
    email = Email(email),
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


