package com.wapps1.redcarga.core.session

import com.wapps1.redcarga.features.auth.domain.models.firebase.FirebaseSession
import com.wapps1.redcarga.features.auth.domain.models.session.AppSession
import com.wapps1.redcarga.features.auth.domain.models.value.Email
import com.wapps1.redcarga.features.auth.domain.models.value.Password
import com.wapps1.redcarga.features.auth.domain.models.value.Platform
import kotlinx.coroutines.flow.StateFlow

/**
 * Store central de autenticación
 * Fuente única de verdad para toda la app
 */
interface AuthSessionStore {
    val sessionState: StateFlow<SessionState>
    val currentUserType: StateFlow<UserType?>
    val currentCompanyId: StateFlow<Long?>  // CompanyId para usuarios PROVIDER
    val currentUsername: StateFlow<String?> // Username del usuario actual

    /**
     * Lee storage seguro y emite el estado inicial (+ auto-login si procede)
     */
    suspend fun bootstrap()

    /**
     * Sign-in manual (email+password) → Firebase → /iam/login → persist & emitir estado
     * Nota: password es String sin validación porque en login la contraseña ya existe en el backend
     */
    suspend fun signInManually(
        email: Email, 
        password: String,  // ✅ String sin validación para login
        platform: Platform, 
        ip: String
    )

    /**
     * Atajo para cuando ya obtuviste FirebaseSession en UI
     */
    suspend fun setFirebaseSession(session: FirebaseSession)

    /**
     * Fuerza intento de /iam/login con Firebase guardado (auto-login)
     */
    suspend fun tryAppLogin(platform: Platform, ip: String)

    /**
     * Limpia TODO (tokens + Room) y emite SignedOut
     */
    suspend fun logout()
}
