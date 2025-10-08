package com.wapps1.redcarga.features.fleet.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "drivers", indices = [Index("companyId"), Index("active")])
data class DriverEntity(
    @PrimaryKey val driverId: Long,
    val companyId: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val licenseNumber: String,
    val active: Boolean,
    val createdAt: Long?,
    val updatedAt: Long?,
    val dirty: Boolean = false,
    val deletedLocally: Boolean = false
)


