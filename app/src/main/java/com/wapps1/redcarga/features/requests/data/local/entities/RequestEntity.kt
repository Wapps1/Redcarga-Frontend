package com.wapps1.redcarga.features.requests.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "requests",
    indices = [
        Index("requesterAccountId"),
        Index("status"),
        Index("createdAt")
    ]
)
data class RequestEntity(
    @PrimaryKey val requestId: Long,
    val requesterAccountId: Long,
    val requesterNameSnapshot: String,
    val requestName: String,
    val requesterDocNumber: String,
    val status: String, // RequestStatus enum as String
    val createdAt: Long, // Instant as Long
    val updatedAt: Long,
    val closedAt: Long?,
    val originDepartmentCode: String,
    val originDepartmentName: String,
    val originProvinceCode: String,
    val originProvinceName: String,
    val originDistrictText: String,
    val destinationDepartmentCode: String,
    val destinationDepartmentName: String,
    val destinationProvinceCode: String,
    val destinationProvinceName: String,
    val destinationDistrictText: String,
    val itemsCount: Int,
    val totalWeightKg: String, // BigDecimal as String
    val paymentOnDelivery: Boolean,
    val dirty: Boolean = false,
    val deletedLocally: Boolean = false
)
