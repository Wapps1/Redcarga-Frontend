package com.wapps1.redcarga.features.auth.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "signup_intent")
data class SignupIntentEntity(
    @PrimaryKey val signupIntentId: Long,
    val accountId: Long,
    val status: String,       // SignupStatus as string
    val expiresAt: Long?,
    val lastStepAt: Long?,
    val verificationSentCount: Int?,
    val lastVerificationSentAt: Long?,
    val platform: String
)
