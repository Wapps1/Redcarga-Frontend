package com.wapps1.redcarga.features.requests.domain.models

import java.time.Instant

data class NewRequestNotification(
    val type: String,
    val requestId: Long,
    val companyId: Int,
    val routeId: Int,
    val routeTypeId: Int,
    val matchKind: String,
    val originDepartmentCode: String,
    val originProvinceCode: String,
    val destDepartmentCode: String,
    val destProvinceCode: String,
    val createdAt: Instant,
    val requesterName: String,
    val originDepartmentName: String,
    val originProvinceName: String,
    val destDepartmentName: String,
    val destProvinceName: String,
    val totalQuantity: Int
) {
    fun getRouteDescription(): String {
        return "$originProvinceName, $originDepartmentName → $destProvinceName, $destDepartmentName"
    }
    
    fun isNewRequest(): Boolean = type == "NEW_REQUEST"
}
