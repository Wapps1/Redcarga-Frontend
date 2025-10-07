package com.wapps1.redcarga.features.auth.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "person_draft")
data class PersonDraftEntity(
    @PrimaryKey val accountId: Long,
    val fullName: String,
    val docTypeCode: String,
    val docNumber: String,
    val birthDate: String,
    val phone: String,
    val ruc: String?
)
