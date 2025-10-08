package com.wapps1.redcarga.features.auth.domain.models.session

import com.wapps1.redcarga.features.auth.domain.models.value.*

/**
 * Sesión de la app para persistencia segura (Encrypted DataStore)
 */
data class AppSession(
    val sessionId: Long,
    val accountId: Long,
    val accessToken: String,
    val expiresAt: Long,
    val tokenType: TokenType,
    val status: SessionStatus,
    val roles: List<RoleCode>,
    val companyId: Long? = null       // Solo presente para usuarios PROVIDER
)
