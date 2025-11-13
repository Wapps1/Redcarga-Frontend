package com.wapps1.redcarga.features.auth.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_snapshot")
data class AccountSnapshotEntity(
    @PrimaryKey val accountId: Long,
    val email: String,
    val username: String,
    val emailVerified: Boolean,
    val status: String,
    val roleCode: String,
    val createdAt: Long?,
    val updatedAt: Long?
)
