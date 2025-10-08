package com.wapps1.redcarga.features.fleet.data.local.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "provinces", indices = [Index("departmentCode")], primaryKeys = ["code"])
data class ProvinceEntity(
    val code: String,
    val departmentCode: String,
    val name: String
)


