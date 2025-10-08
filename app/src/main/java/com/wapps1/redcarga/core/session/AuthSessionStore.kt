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

    /**
     * Lee storage seguro y emite el estado inicial (+ auto-login si procede)
     */
    suspend fun bootstrap()

    /**
     * Sign-in manual (email+password) → Firebase → /iam/login → persist & emitir estado
     */
    suspend fun signInManually(
        email: Email, 
        password: Password, 
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
