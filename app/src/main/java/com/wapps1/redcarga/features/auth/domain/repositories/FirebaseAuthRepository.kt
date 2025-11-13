package com.wapps1.redcarga.features.auth.domain.repositories

import com.wapps1.redcarga.features.auth.domain.models.firebase.FirebaseSession
import com.wapps1.redcarga.features.auth.domain.models.value.Email

/**
 * Repositorio para autenticación con Firebase
 */
interface FirebaseAuthRepository {
    suspend fun signInWithPassword(email: Email, password: String): FirebaseSession  // ✅ String para login sin validación
}
