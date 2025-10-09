package com.wapps1.redcarga.features.requests.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "request_items",
    indices = [Index("requestId")],
    foreignKeys = [
        ForeignKey(
            entity = RequestEntity::class,
            parentColumns = ["requestId"],
            childColumns = ["requestId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RequestItemEntity(
    @PrimaryKey(autoGenerate = true) val itemId: Long,
    val requestId: Long,
    val itemName: String,
    val heightCm: String, // BigDecimal as String
    val widthCm: String,
    val lengthCm: String,
    val weightKg: String,
    val totalWeightKg: String,
    val quantity: Int,
    val fragile: Boolean,
    val notes: String,
    val position: Int
)
