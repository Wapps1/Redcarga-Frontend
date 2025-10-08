package com.wapps1.redcarga.features.fleet.data.remote.models

data class RouteDto(
    val routeId: Long,
    val companyId: Long,
    val companyName: String?,
    val routeType: String,
    val originDepartmentCode: String,
    val originProvinceCode: String?,
    val destDepartmentCode: String,
    val destProvinceCode: String?,
    val originDepartmentName: String?,
    val originProvinceName: String?,
    val destDepartmentName: String?,
    val destProvinceName: String?,
    val active: Boolean
)

data class RouteCreateDto(
    val routeTypeId: Int,
    val originDepartmentCode: String,
    val destDepartmentCode: String,
    val originProvinceCode: String?,
    val destProvinceCode: String?,
    val active: Boolean
)

data class RouteUpdateDto(
    val routeTypeId: Int,
    val originDepartmentCode: String,
    val originProvinceCode: String?,
    val destDepartmentCode: String,
    val destProvinceCode: String?,
    val active: Boolean
)

data class CreateRouteResponseDto(val success: Boolean, val routeId: Long)


