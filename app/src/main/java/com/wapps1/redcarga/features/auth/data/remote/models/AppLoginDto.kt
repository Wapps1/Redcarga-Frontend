package com.wapps1.redcarga.features.auth.data.remote.models

data class AppLoginRequestDto(
    val platform: String,
    val ip: String
)

data class AppLoginResponseDto(
    val sessionId: Long,
    val accountId: Long,
    val accessToken: String,
    val expiresIn: Long,     // segundos
    val expiresAt: Long?,    // timestamp absoluto (puede ser null si backend viejo)
    val tokenType: String,   // "Bearer"
    val status: String,      // "ACTIVE" | "REVOKED" | "EXPIRED"
    val roles: List<String>?, // ["CLIENT", "PROVIDER"] (opcional por compat)
    val account: AccountLightDto? // snapshot ligero (opcional por compat)
)

data class AccountLightDto(
    val username: String,
    val email: String,
    val emailVerified: Boolean,
    val updatedAt: Long,
    val companyId: Long? = null  // Solo presente para PROVIDER
)
