package com.wapps1.redcarga.features.auth.data.remote.models

data class FirebaseSignInRequestDto(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean = true
)

data class FirebaseSignInResponseDto(
    val localId: String,
    val email: String,
    val idToken: String,
    val refreshToken: String,
    val expiresIn: String     // "3600"
)
