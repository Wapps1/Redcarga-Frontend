package com.wapps1.redcarga.features.fleet.data.local.entities

import androidx.room.Entity

@Entity(tableName = "meta", primaryKeys = ["key"])
data class MetaEntity(
    val key: String,
    val value: Long
)


