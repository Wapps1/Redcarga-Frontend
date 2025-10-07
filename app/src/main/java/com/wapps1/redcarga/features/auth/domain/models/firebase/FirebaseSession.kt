package com.wapps1.redcarga.features.auth.domain.models.firebase

import com.wapps1.redcarga.features.auth.domain.models.value.Email

/**
 * Sesión de Firebase para persistencia segura (Encrypted DataStore)
 */
data class FirebaseSession(
    val localId: String,
    val email: Email,
    val idToken: String,
    val refreshToken: String,
    val expiresAt: Long // timestamp de expiración
)
