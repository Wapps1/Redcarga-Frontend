package com.wapps1.redcarga.features.fleet.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "pending_ops", indices = [Index("resourceType"), Index("status")])
data class PendingOperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val resourceType: String,
    val operation: String,
    val resourceId: Long?,
    val companyId: Long?,
    val payloadJson: String,
    val status: String,
    val attempts: Int = 0,
    val createdAt: Long,
    val lastAttemptAt: Long? = null
)


