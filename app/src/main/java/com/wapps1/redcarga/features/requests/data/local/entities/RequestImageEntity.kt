package com.wapps1.redcarga.features.requests.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "request_images",
    indices = [Index("itemId")],
    foreignKeys = [
        ForeignKey(
            entity = RequestItemEntity::class,
            parentColumns = ["itemId"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RequestImageEntity(
    @PrimaryKey(autoGenerate = true) val imageId: Long,
    val itemId: Long,
    val imageUrl: String,
    val imagePosition: Int
)
 