package com.wapps1.redcarga.features.fleet.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles", indices = [Index("companyId"), Index("active")])
data class VehicleEntity(
    @PrimaryKey val vehicleId: Long,
    val companyId: Long,
    val name: String,
    val plate: String,
    val active: Boolean,
    val createdAt: Long?,
    val updatedAt: Long?,
    val dirty: Boolean = false,
    val deletedLocally: Boolean = false
)


