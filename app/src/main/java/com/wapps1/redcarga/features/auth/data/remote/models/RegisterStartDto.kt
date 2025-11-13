package com.wapps1.redcarga.features.auth.data.remote.models

data class RegisterStartRequestDto(
    val email: String,
    val username: String,
    val password: String,
    val roleCode: String,  // "CLIENT" | "PROVIDER"
    val platform: String   // "ANDROID" | "WEB"
)

data class RegisterStartResponseDto(
    val accountId: Long,
    val signupIntentId: Long,
    val email: String,
    val emailVerified: Boolean,
    val verificationLink: String
)
