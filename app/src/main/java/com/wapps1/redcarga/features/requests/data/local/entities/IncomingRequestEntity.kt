package com.wapps1.redcarga.features.requests.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity de Room para solicitudes entrantes al inbox de un proveedor
 */
@Entity(tableName = "incoming_requests")
data class IncomingRequestEntity(
    @PrimaryKey val requestId: Long,
    val companyId: Long,
    val matchedRouteId: Long,
    val routeTypeId: Long,
    val status: String,
    val createdAt: String, // ISO 8601 string
    val requesterName: String,
    val originDepartmentName: String,
    val originProvinceName: String,
    val destDepartmentName: String,
    val destProvinceName: String,
    val totalQuantity: Int
)

