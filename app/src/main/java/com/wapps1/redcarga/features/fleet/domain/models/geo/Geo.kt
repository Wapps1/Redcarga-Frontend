package com.wapps1.redcarga.features.fleet.domain.models.geo

data class Department(
    val code: String,
    val name: String
)

data class Province(
    val code: String,
    val departmentCode: String,
    val name: String
)

data class GeoCatalog(
    val departments: List<Department>,
    val provinces: List<Province>
)


