package com.wapps1.redcarga.features.fleet.data.remote.models

data class GeoCatalogDto(
    val departments: List<DepartmentDto>,
    val provinces: List<ProvinceDto>
)

data class DepartmentDto(val code: String, val name: String)

data class ProvinceDto(val code: String, val departmentCode: String, val name: String)


