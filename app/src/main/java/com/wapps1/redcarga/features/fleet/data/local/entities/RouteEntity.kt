package com.wapps1.redcarga.features.fleet.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "routes", indices = [Index("companyId"), Index("routeType"), Index("active")])
data class RouteEntity(
    @PrimaryKey val routeId: Long,
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
    val active: Boolean,
    val dirty: Boolean = false,
    val deletedLocally: Boolean = false
)


