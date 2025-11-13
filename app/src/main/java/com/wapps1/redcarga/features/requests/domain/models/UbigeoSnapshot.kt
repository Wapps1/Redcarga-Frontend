package com.wapps1.redcarga.features.requests.domain.models

data class UbigeoSnapshot(
    val departmentCode: String,
    val departmentName: String,
    val provinceCode: String,
    val provinceName: String,
    val districtText: String
) {
    fun getFullLocation(): String {
        return "$districtText, $provinceName, $departmentName"
    }
    
    fun getShortLocation(): String {
        return "$provinceName, $departmentName"
    }
}
