package com.wapps1.redcarga.features.fleet.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "departments")
data class DepartmentEntity(
    @PrimaryKey val code: String,
    val name: String
)


